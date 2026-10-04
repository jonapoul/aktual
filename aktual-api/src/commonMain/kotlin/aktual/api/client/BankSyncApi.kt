package aktual.api.client

import aktual.api.model.banksync.BankSyncAccountsResponse
import aktual.api.model.banksync.BankSyncStatusResponse
import aktual.api.model.banksync.BankSyncTransactionsRequest
import aktual.api.model.banksync.BankSyncTransactionsResponse
import aktual.api.model.banksync.SimpleFinBatchRequest
import aktual.api.model.banksync.SimpleFinBatchResponse
import aktual.budget.model.AccountSyncSource

interface BankSyncApi {
  suspend fun status(source: AccountSyncSource): BankSyncStatusResponse

  suspend fun transactions(
    source: AccountSyncSource,
    request: BankSyncTransactionsRequest,
  ): BankSyncTransactionsResponse

  suspend fun simpleFinBatch(request: SimpleFinBatchRequest): SimpleFinBatchResponse

  /**
   * Lists the accounts [source] can see at the user's banks, for linking to budget accounts. Only
   * SimpleFIN, Pluggy.ai and Akahu: GoCardless and Enable Banking list them after a bank login
   * instead. Gives up after a minute.
   */
  suspend fun accounts(source: AccountSyncSource): BankSyncAccountsResponse

  /**
   * Deletes a GoCardless bank login once no accounts are linked through it. False if GoCardless
   * refused.
   */
  suspend fun removeGoCardlessRequisition(requisitionId: String): Boolean
}
