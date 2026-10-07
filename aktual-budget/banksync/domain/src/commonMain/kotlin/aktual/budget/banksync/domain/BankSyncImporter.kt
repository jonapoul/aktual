@file:Suppress("UnnecessaryParentheses")

package aktual.budget.banksync.domain

import aktual.api.model.banksync.BankSyncTransaction
import aktual.budget.db.dao.BankSyncCandidate
import aktual.budget.db.dao.BankSyncDao
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSyncSource
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.TransactionId
import aktual.budget.rules.domain.LoadedRules
import aktual.budget.rules.domain.RuleTransaction
import aktual.budget.rules.domain.TransactionRulesLoader
import aktual.budget.transactions.domain.AccountUpdate
import aktual.budget.transactions.domain.NewTransaction
import aktual.budget.transactions.domain.Patch
import aktual.budget.transactions.domain.TransactionBatch
import aktual.budget.transactions.domain.TransactionUpdate
import aktual.budget.transactions.domain.TransactionWriter
import aktual.core.Calendar
import aktual.core.UuidGenerator
import dev.zacsweers.metro.Inject
import kotlin.math.abs
import kotlin.math.floor
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import logcat.logcat

data class BankSyncDownload(
  val transactions: List<BankSyncTransaction>,
  val currentBalance: Amount? = null,
)

data class BankSyncImportResult(val added: List<TransactionId>, val updated: List<TransactionId>)

/**
 * Saves downloaded bank transactions into the open budget: processBankSyncDownload() and
 * reconcileTransactions() in packages/loot-core/src/server/accounts/sync.ts, for bank sync only.
 * Each download is normalized, run through the rules, then matched against existing transactions,
 * first by imported_id and then by amount within a week. Matches fill in what they're missing and
 * the rest are added, all in one [TransactionWriter.write] batch.
 *
 * Two differences from upstream: a matched transaction only gets messages for the fields that
 * changed, and payees are only created if something ends up using them.
 */
@Inject
class BankSyncImporter(
  private val writer: TransactionWriter,
  private val rulesLoader: TransactionRulesLoader,
  private val settingsLoader: BankSyncSettingsLoader,
  private val dao: BankSyncDao,
  private val uuidGenerator: UuidGenerator,
  private val calendar: Calendar,
  private val clock: Clock,
) {
  /**
   * The first sync of an account adds a starting balance that, with the downloaded transactions,
   * adds up to [BankSyncDownload.currentBalance]. Later ones update the account's balance instead.
   */
  suspend fun import(
    account: AccountId,
    source: AccountSyncSource?,
    download: BankSyncDownload,
    initialSync: Boolean = false,
    customStartingBalance: Amount? = null,
    customStartingDate: LocalDate? = null,
  ): BankSyncImportResult {
    val settings = settingsLoader.load(account)
    val options = Options(account, settings, strictIdChecking = source == null)
    val result = writer.write {
      if (initialSync) {
        val balance =
          customStartingBalance ?: startingBalance(source, download, settings.importPending)
        val date =
          customStartingDate
            ?: download.transactions.lastOrNull()?.date?.let(LocalDate::parse)
            ?: calendar.today()
        // Upstream inserts this before matching, so it could match a downloaded transaction. Here
        // it's only saved with the rest of the batch
        val startingBalance = insertStartingBalance(account, balance, date)
        val reconciled = reconcile(download.transactions, options, setAccount = false)
        reconciled.copy(added = listOf(startingBalance) + reconciled.added)
      } else {
        val transactions = if (settings.importTransactions) download.transactions else emptyList()
        val reconciled = reconcile(transactions, options, setAccount = true)
        download.currentBalance?.let { balance ->
          updateAccount(AccountUpdate(account, balanceCurrent = Patch.To(balance)))
        }
        reconciled
      }
    }
    logcat.i { "Bank sync added ${result.added.size}, updated ${result.updated.size} in $account" }
    return result
  }

  private suspend fun TransactionBatch.reconcile(
    transactions: List<BankSyncTransaction>,
    options: Options,
    setAccount: Boolean,
  ): BankSyncImportResult {
    val rules = rulesLoader.load()
    val categoryIds = dao.categoryIds()
    val normalized = transactions.mapNotNull {
      normalize(it, options, rules, categoryIds, setAccount)
    }
    val matched = match(normalized, options, rules)

    val inserts = mutableListOf<NewTransaction>()
    val updates = mutableListOf<TransactionUpdate>()
    for (transaction in matched) {
      val match = transaction.match
      when {
        // Already reconciled (locked) transactions are left alone
        match != null -> if (!match.reconciled) updates += updatesFor(transaction, match, options)
        // Deleted by a rule
        transaction.rules.tombstone -> Unit
        else -> inserts += newTransactions(transaction)
      }
    }

    // Keep the server's order
    val now = clock.now().toEpochMilliseconds()
    val added = inserts.mapIndexed { index, t ->
      t.copy(sortOrder = t.sortOrder ?: (now - index * TRANSACTION_SORT_INCREMENT))
    }

    // createNewPayees()
    val usedPayees =
      added.mapNotNull { it.payee }.toSet() + updates.mapNotNull { (it.payee as? Patch.To)?.value }
    for ((id, name) in rules.createdPayees) {
      if (id in usedPayees) insertPayee(name, id = id)
    }

    added.forEach { insert(it) }
    updates.forEach { update(it) }
    return BankSyncImportResult(added = added.mapNotNull { it.id }, updated = updates.map { it.id })
  }

  // normalizeBankSyncTransactions()
  private fun normalize(
    download: BankSyncTransaction,
    options: Options,
    rules: LoadedRules,
    categoryIds: Set<CategoryId>,
    setAccount: Boolean,
  ): Normalized? {
    val settings = options.settings
    // Upstream changes the downloaded object as it goes, then saves it as raw_synced_data. A
    // LinkedHashMap keeps the keys in the same order as JSON.stringify() would
    val json = LinkedHashMap<String, JsonElement>(download.json)
    fun string(key: String?) = key?.let { (json[it] as? JsonPrimitive)?.contentOrNull }

    if (setAccount) json[ACCOUNT] = JsonPrimitive(options.account.value)
    val cleared = download.isBooked
    json["cleared"] = JsonPrimitive(cleared)
    if (!settings.importPending && !cleared) return null

    if (!json[AMOUNT].isTruthy()) {
      json[AMOUNT] = download.json["transactionAmount"]?.jsonObject?.get(AMOUNT) ?: JsonNull
    }
    val amount =
      requireNotNull((json[AMOUNT] as? JsonPrimitive)?.contentOrNull?.toDoubleOrNull()) {
        "Invalid amount: ${json[AMOUNT]}"
      }

    val mapping = if (amount <= 0) settings.mappings.payment else settings.mappings.deposit
    val dateString =
      requireNotNull(string(mapping.date) ?: download.date) {
        "`date` is required when adding a transaction"
      }
    val payeeName =
      requireNotNull(string(mapping.payee) ?: download.payeeName) {
        "`payeeName` is required when adding a transaction"
      }
    val notes = string(mapping.notes)

    val importedPayee = (string(IMPORTED_PAYEE)?.ifEmpty { null } ?: payeeName).trim()
    json[IMPORTED_PAYEE] = JsonPrimitive(importedPayee)

    val transactionId = download.transactionId
    val internalId = download.internalTransactionId
    val importedId =
      if (cleared && transactionId.isNullOrEmpty() && !internalId.isNullOrEmpty()) {
        // On a first sync the account is whatever the provider sent, usually "undefined"
        "${json[ACCOUNT].jsString()}-$internalId"
      } else {
        transactionId
      }

    json[ACCOUNT] = JsonPrimitive(options.account.value)
    val payee = payeeName.ifEmpty { null }?.let(rules::resolvePayee)
    if (payee != null) json["payee"] = JsonPrimitive(payee.value)

    val transaction =
      RuleTransaction(
        account = options.account,
        date = LocalDate.parse(dateString),
        amount = Amount(jsRound(amount * CENTS)),
        payee = payee,
        importedPayee = importedPayee,
        category = string("category")?.let(::CategoryId)?.takeIf { it in categoryIds },
        notes =
          notes?.takeIf { settings.importNotes && it.isNotEmpty() }?.trim()?.replace("#", "##"),
        cleared = cleared,
      )
    val raw = Json.encodeToString(JsonObject.serializer(), JsonObject(json))
    return Normalized(transaction, importedId, raw)
  }

  // matchTransactions()
  private suspend fun match(
    normalized: List<Normalized>,
    options: Options,
    rules: LoadedRules,
  ): List<Matched> {
    val hasMatched = mutableSetOf<TransactionId>()
    val exactMatchedParents = mutableSetOf<TransactionId>()

    // Run the rules, then match on imported_id, the most reliable match
    val matched = normalized.map { n ->
      val transaction = rules.run(n.transaction)
      val exact =
        n.importedId
          ?.ifEmpty { null }
          ?.let { dao.matchByImportedId(it, options.account, !options.settings.reimportDeleted) }
      if (exact != null) {
        hasMatched += exact.id
        if (exact.isParent) exactMatchedParents += exact.id
      }
      val fuzzy =
        if (exact == null) fuzzyCandidates(transaction, n.importedId, options) else emptyList()
      Matched(transaction, n.importedId, n.rawSyncedData, fuzzy, exact)
    }

    // Then on amount and date, first those with the same payee, then any. Children of a split
    // that matched exactly are already accounted for
    fun BankSyncCandidate.isAvailable(): Boolean {
      val parent = parentId
      return id !in hasMatched && (parent == null || parent !in exactMatchedParents)
    }
    val matches = matched.mapTo(mutableListOf()) { it.match }
    fun fuzzyPass(predicate: (Matched, BankSyncCandidate) -> Boolean) {
      matched.forEachIndexed { index, m ->
        if (matches[index] != null) return@forEachIndexed
        val match = m.fuzzy.firstOrNull { it.isAvailable() && predicate(m, it) }
        matches[index] = match
        match?.let { hasMatched += it.id }
      }
    }
    fuzzyPass { m, candidate -> candidate.payee == m.rules.payee }
    fuzzyPass { _, _ -> true }
    return matched.mapIndexed { index, m -> m.copy(match = matches[index]) }
  }

  private suspend fun fuzzyCandidates(
    transaction: RuleTransaction,
    importedId: String?,
    options: Options,
  ): List<BankSyncCandidate> {
    val date = transaction.date
    val candidates =
      dao.fuzzyCandidates(
        account = options.account,
        amount = transaction.amount.toLong(),
        from = date.minus(FUZZY_DAYS, DateTimeUnit.DAY),
        to = date.plus(FUZZY_DAYS, DateTimeUnit.DAY),
        // Both having different imported_ids means they're different transactions
        onlyWithoutImportedId = options.strictIdChecking && !importedId.isNullOrEmpty(),
      )
    return candidates.sortedWith(fuzzyMatchOrder(date))
  }

  // The match branch of reconcileTransactions()
  private suspend fun updatesFor(
    transaction: Matched,
    existing: BankSyncCandidate,
    options: Options,
  ): List<TransactionUpdate> {
    val t = transaction.rules
    val cleared = existing.cleared || t.cleared
    val date = t.date.takeIf { options.settings.updateDates }
    val update =
      TransactionUpdate(
        id = existing.id,
        importedId = patch(existing.importedId, transaction.importedId?.ifEmpty { null }),
        payee = patch(existing.payee, existing.payee ?: t.payee),
        category = patch(existing.category, existing.category ?: t.category),
        importedPayee = patch(existing.importedPayee, t.importedPayee?.ifEmpty { null }),
        notes =
          patch(existing.notes, existing.notes?.ifEmpty { null } ?: t.notes?.ifEmpty { null }),
        cleared = cleared.takeIf { it != existing.cleared },
        rawSyncedData = patch(existing.rawSyncedData, existing.rawSyncedData ?: transaction.raw),
        date = date?.takeIf { it != existing.date },
      )

    val updates = mutableListOf<TransactionUpdate>()
    if (update != TransactionUpdate(existing.id)) updates += update

    // A split's children follow the parent's cleared flag and date
    if (existing.isParent && (update.cleared != null || update.date != null)) {
      updates +=
        dao.childIds(existing.id).map { child ->
          TransactionUpdate(id = child, cleared = update.cleared, date = update.date)
        }
    }
    return updates
  }

  // The insert branch of reconcileTransactions(), with makeSplitTransaction() for splits
  private fun newTransactions(transaction: Matched): List<NewTransaction> {
    val t = transaction.rules
    val children = t.subtransactions
    val isSplit = children.isNotEmpty()
    val id = uuidGenerator(::TransactionId)
    val difference = t.amount.toLong() - children.sumOf { it.amount.toLong() }
    val parent =
      NewTransaction(
        id = id,
        account = t.account,
        date = t.date,
        amount = t.amount,
        payee = t.payee,
        category = t.category,
        notes = t.notes,
        importedId = transaction.importedId,
        importedPayee = t.importedPayee,
        cleared = t.cleared,
        reconciled = t.reconciled,
        schedule = t.schedule,
        isParent = isSplit,
        error = if (isSplit && difference != 0L) splitError(difference) else null,
        rawSyncedData = transaction.raw,
      )
    return listOf(parent) +
      children.mapIndexed { index, child ->
        NewTransaction(
          id = uuidGenerator(::TransactionId),
          account = t.account,
          date = t.date,
          amount = child.amount,
          payee = child.payee,
          category = child.category,
          notes = child.notes,
          cleared = t.cleared,
          reconciled = t.reconciled,
          parentId = id,
          sortOrder = -index.toLong(),
        )
      }
  }

  private data class Options(
    val account: AccountId,
    val settings: BankSyncSettings,
    val strictIdChecking: Boolean,
  )

  private data class Normalized(
    val transaction: RuleTransaction,
    val importedId: String?,
    val rawSyncedData: String,
  )

  private data class Matched(
    // After the rules ran
    val rules: RuleTransaction,
    val importedId: String?,
    val raw: String,
    val fuzzy: List<BankSyncCandidate>,
    val match: BankSyncCandidate?,
  )
}

internal fun fuzzyMatchOrder(date: LocalDate): Comparator<BankSyncCandidate> =
  compareBy({ abs(it.date.daysUntil(date)) }, { it.importedId != null })

internal fun startingBalance(
  source: AccountSyncSource?,
  download: BankSyncDownload,
  importPending: Boolean,
): Amount {
  val current = download.currentBalance?.toLong() ?: 0L
  val transactions = download.transactions
  fun minusAll(transactions: List<BankSyncTransaction>) =
    current - transactions.sumOf { jsRound(it.decimalAmount() * CENTS) }
  val balance =
    when (source) {
      SimpleFin,
      Akahu -> minusAll(transactions)
      EnableBanking ->
        minusAll(if (importPending) transactions else transactions.filter { it.isBooked })
      PluggyAi ->
        jsRound(
          transactions.fold(current.toDouble()) { total, t -> total - t.decimalAmount() * CENTS },
        )
      else -> current
    }
  return Amount(balance)
}

private fun BankSyncTransaction.decimalAmount(): Double =
  amount?.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid amount: $amount")

private fun splitError(difference: Long): JsonObject = buildJsonObject {
  put("type", "SplitTransactionError")
  put("version", 1)
  put("difference", difference)
}

private fun <T> patch(old: T?, new: T?): Patch<T?> = if (old == new) Keep else Patch.To(new)

// Math.round()
private fun jsRound(value: Double): Long = floor(value + HALF).toLong()

// JavaScript truthiness, for the values a provider might send
private fun JsonElement?.isTruthy(): Boolean =
  when (this) {
    null,
    JsonNull -> false
    is JsonPrimitive ->
      if (isString) {
        content.isNotEmpty()
      } else {
        booleanOrNull ?: (doubleOrNull?.let { it != 0.0 && !it.isNaN() } != false)
      }
    is JsonObject,
    is JsonArray -> true
  }

// String() of a field read from the provider's object
private fun JsonElement?.jsString(): String =
  when (this) {
    null -> "undefined"
    is JsonPrimitive -> content
    is JsonObject,
    is JsonArray -> toString()
  }

private const val ACCOUNT = "account"
private const val AMOUNT = "amount"
private const val IMPORTED_PAYEE = "imported_payee"
private const val CENTS = 100
private const val HALF = 0.5
private const val FUZZY_DAYS = 7
private const val TRANSACTION_SORT_INCREMENT = 1024L
