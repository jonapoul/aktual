package aktual.budget.banksync.vm

import aktual.api.client.BankSyncApi
import aktual.api.model.banksync.BankSyncAccountsResponse
import aktual.api.model.banksync.BankSyncStatusResponse
import aktual.api.model.banksync.BankSyncTransactionsRequest
import aktual.api.model.banksync.BankSyncTransactionsResponse
import aktual.api.model.banksync.GoCardlessAccountsResponse
import aktual.api.model.banksync.GoCardlessBanksResponse
import aktual.api.model.banksync.GoCardlessLoginResponse
import aktual.api.model.banksync.SimpleFinBatchRequest
import aktual.api.model.banksync.SimpleFinBatchResponse
import aktual.budget.model.AccountSyncSource

// Answers status, account list and GoCardless requests from what the test gives it
internal class FakeBankSyncApi(vararg statuses: Pair<AccountSyncSource, BankSyncStatusResponse>) :
  BankSyncApi {
  val statuses = statuses.toMap().toMutableMap()
  val requested = mutableListOf<AccountSyncSource>()
  val accounts = mutableMapOf<AccountSyncSource, BankSyncAccountsResponse>()
  val listed = mutableListOf<AccountSyncSource>()
  val removedRequisitions = mutableListOf<String>()
  val banks = mutableMapOf<String, GoCardlessBanksResponse>()
  val bankRequests = mutableListOf<String>()
  var login: GoCardlessLoginResponse? = null
  val logins = mutableListOf<String>()
  val polls = ArrayDeque<GoCardlessAccountsResponse>()

  override suspend fun status(source: AccountSyncSource): BankSyncStatusResponse {
    requested += source
    return statuses[source] ?: error("No status for $source")
  }

  override suspend fun transactions(
    source: AccountSyncSource,
    request: BankSyncTransactionsRequest,
  ): BankSyncTransactionsResponse = error("Not used")

  override suspend fun simpleFinBatch(request: SimpleFinBatchRequest): SimpleFinBatchResponse =
    error("Not used")

  override suspend fun accounts(source: AccountSyncSource): BankSyncAccountsResponse {
    listed += source
    return accounts[source] ?: error("No accounts for $source")
  }

  override suspend fun goCardlessBanks(
    country: String,
    showDemo: Boolean,
  ): GoCardlessBanksResponse {
    bankRequests += country
    return banks[country] ?: error("No banks for $country")
  }

  override suspend fun goCardlessLogin(bankId: String): GoCardlessLoginResponse {
    logins += bankId
    return checkNotNull(login)
  }

  // Pending once the queue's empty
  override suspend fun goCardlessAccounts(requisitionId: String): GoCardlessAccountsResponse =
    polls.removeFirstOrNull() ?: Pending

  override suspend fun removeGoCardlessRequisition(requisitionId: String): Boolean {
    removedRequisitions += requisitionId
    return true
  }
}
