package aktual.budget.banksync.vm

import aktual.api.client.BankSyncApi
import aktual.api.model.banksync.BankSyncAccountsResponse
import aktual.api.model.banksync.BankSyncStatusResponse
import aktual.api.model.banksync.BankSyncTransactionsRequest
import aktual.api.model.banksync.BankSyncTransactionsResponse
import aktual.api.model.banksync.SimpleFinBatchRequest
import aktual.api.model.banksync.SimpleFinBatchResponse
import aktual.budget.model.AccountSyncSource

// Answers status and account list requests from what the test gives it
internal class FakeBankSyncApi(vararg statuses: Pair<AccountSyncSource, BankSyncStatusResponse>) :
  BankSyncApi {
  private val statuses = statuses.toMap()
  val requested = mutableListOf<AccountSyncSource>()
  val accounts = mutableMapOf<AccountSyncSource, BankSyncAccountsResponse>()
  val listed = mutableListOf<AccountSyncSource>()
  val removedRequisitions = mutableListOf<String>()

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

  override suspend fun removeGoCardlessRequisition(requisitionId: String): Boolean {
    removedRequisitions += requisitionId
    return true
  }
}
