package aktual.api.client

import aktual.api.model.banksync.ExternalBankAccount
import aktual.budget.model.AccountSyncSource
import aktual.budget.model.Amount
import kotlin.math.roundToLong
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull

// Each provider's accounts in the shape SelectLinkedAccountsModal takes, as normalised in
// packages/desktop-client/src/hooks/useBuiltInBankSyncProviders.ts. Entries without an ID are
// dropped
internal fun externalAccounts(
  source: AccountSyncSource,
  accounts: JsonArray,
): List<ExternalBankAccount> = accounts.mapNotNull { element ->
  val json = element as? JsonObject ?: return@mapNotNull null
  when (source) {
    AccountSyncSource.SimpleFin -> simpleFin(json)
    AccountSyncSource.PluggyAi -> pluggyAi(json)
    AccountSyncSource.Akahu -> akahu(json)
    else -> throw IllegalArgumentException("$source doesn't list accounts")
  }
}

// The accounts shared through a GoCardless login, which is the bank's ID. See
// SyncServerGoCardlessAccount in packages/loot-core/src/types/models/gocardless.ts, whose
// institution is either a name or an object with one
internal fun goCardlessAccounts(
  requisitionId: String,
  accounts: JsonArray,
): List<ExternalBankAccount> = accounts.mapNotNull { element ->
  val json = element as? JsonObject ?: return@mapNotNull null
  val institution = json["institution"]
  ExternalBankAccount(
    accountId = json.string("account_id") ?: return@mapNotNull null,
    name = json.string(NAME).orEmpty(),
    institution =
      (institution as? JsonObject)?.string(NAME) ?: institution.primitive()?.contentOrNull,
    orgId = requisitionId,
    orgDomain = null,
    balance = null,
  )
}

// The accounts shared through an Enable Banking login, normalised by the server. Each is its own
// bank, as linkEnableBankingAccount() in packages/loot-core/src/server/accounts/app.ts stores
// them, and their balances are in cents
internal fun enableBankingAccounts(accounts: JsonArray): List<ExternalBankAccount> =
  accounts.mapNotNull { element ->
    val json = element as? JsonObject ?: return@mapNotNull null
    val id = json.string("account_id") ?: return@mapNotNull null
    ExternalBankAccount(
      accountId = id,
      name = json.string(NAME).orEmpty(),
      institution = json.string("institution"),
      orgId = id,
      orgDomain = null,
      balance = json.number(BALANCE)?.roundToLong()?.let(::Amount),
    )
  }

private fun simpleFin(json: JsonObject): ExternalBankAccount? {
  val org = json["org"] as? JsonObject
  return ExternalBankAccount(
    accountId = json.string(ID) ?: return null,
    name = json.string(NAME).orEmpty(),
    institution = org?.string(NAME),
    orgId = org?.string(ID),
    orgDomain = org?.string("domain"),
    balance = json.number(BALANCE)?.let(::Amount),
  )
}

private fun pluggyAi(json: JsonObject): ExternalBankAccount? {
  val isBank = json.string("type") == "BANK"
  val bankData = json["bankData"] as? JsonObject
  val name = json.string(NAME).orEmpty().trim()
  val suffix = if (isBank) json.string("taxNumber") else json.string("owner")
  val balance =
    if (isBank) {
      val invested = bankData?.number("automaticallyInvestedBalance")
      val closing = bankData?.number("closingBalance")
      if (invested == null && closing == null) null else (invested ?: 0.0) + (closing ?: 0.0)
    } else {
      json.number(BALANCE)
    }
  return ExternalBankAccount(
    accountId = json.string(ID) ?: return null,
    name = "$name - ${suffix.orEmpty()}",
    institution = name,
    orgId = json.string(ID),
    orgDomain = null,
    balance = balance?.let(::Amount),
  )
}

private fun akahu(json: JsonObject): ExternalBankAccount? {
  val connection = json["connection"] as? JsonObject
  return ExternalBankAccount(
    accountId = json.string(AKAHU_ID) ?: return null,
    name = json.string(NAME).orEmpty(),
    institution = connection?.string(NAME),
    orgId = connection?.string(AKAHU_ID),
    orgDomain = connection?.string(NAME),
    balance = (json[BALANCE] as? JsonObject)?.number("current")?.let(::Amount),
  )
}

private fun JsonObject.string(key: String): String? = get(key).primitive()?.contentOrNull

// SimpleFIN sends balances as decimal strings, the others as numbers
private fun JsonObject.number(key: String): Double? =
  get(key).primitive()?.let { p ->
    p.doubleOrNull ?: p.contentOrNull?.toDoubleOrNull()
  }

private fun JsonElement?.primitive(): JsonPrimitive? = this as? JsonPrimitive

private const val NAME = "name"
private const val ID = "id"
private const val AKAHU_ID = "_id"
private const val BALANCE = "balance"
