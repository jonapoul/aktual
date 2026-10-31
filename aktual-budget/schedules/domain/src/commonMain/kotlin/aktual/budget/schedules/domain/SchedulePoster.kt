package aktual.budget.schedules.domain

import aktual.budget.db.schedules.GetAllActive
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.TransactionId
import aktual.budget.model.parseScheduleAmount
import aktual.budget.rules.domain.LoadedRules
import aktual.budget.rules.domain.RuleTransaction
import aktual.budget.rules.domain.TransactionRulesLoader
import aktual.budget.transactions.domain.TransactionWriter
import aktual.budget.transactions.domain.toNewTransactions
import aktual.core.UuidGenerator
import dev.zacsweers.metro.Inject
import kotlinx.datetime.LocalDate
import logcat.logcat

// Adds schedule transactions through the rules, as addTransactions() in
// packages/loot-core/src/server/accounts/sync.ts does
@Inject
internal class SchedulePoster(
  private val rulesLoader: TransactionRulesLoader,
  private val transactionWriter: TransactionWriter,
  private val uuidGenerator: UuidGenerator,
) {
  fun batch(): Batch = Batch()

  inner class Batch {
    private var rules: LoadedRules? = null
    private val transactions = mutableListOf<RuleTransaction>()

    // What will be posted after the rules ran, or null if a rule deleted it
    suspend fun add(transaction: RuleTransaction): RuleTransaction? {
      val loaded = rules ?: rulesLoader.load().also { rules = it }
      val result = loaded.run(transaction)
      if (result.tombstone) return null
      transactions += result
      return result
    }

    // Gives the IDs of the posted transactions, without split children
    suspend fun post(): List<TransactionId> {
      if (transactions.isEmpty()) return emptyList()
      val rows = transactions.flatMap {
        it.toNewTransactions(newId = { uuidGenerator(::TransactionId) })
      }
      val usedPayees = rows.mapNotNull { it.payee }.toSet()
      transactionWriter.write(runTransfers = true) {
        for ((id, name) in rules?.createdPayees.orEmpty()) {
          if (id in usedPayees) insertPayee(name, id = id)
        }
        rows.forEach { insert(it) }
      }
      logcat.i { "Posted schedules: ${transactions.mapNotNull { it.schedule }.distinct()}" }
      return rows.filter { it.parentId == null }.mapNotNull { it.id }
    }
  }
}

// The transaction postTransactionForSchedule() builds for [date], or null without an account
internal fun GetAllActive.toRuleTransaction(date: LocalDate): RuleTransaction? {
  val account = _account?.let(::AccountId) ?: return null
  return RuleTransaction(
    account = account,
    date = date,
    amount = Amount(parseScheduleAmount(_amount) ?: 0L),
    payee = _payee,
    schedule = id,
    cleared = false,
  )
}
