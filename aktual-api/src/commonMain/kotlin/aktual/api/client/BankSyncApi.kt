package aktual.api.client

import aktual.api.model.banksync.BankSyncStatusResponse
import aktual.api.model.banksync.BankSyncTransactionsRequest
import aktual.api.model.banksync.BankSyncTransactionsResponse
import aktual.api.model.banksync.SimpleFinBatchRequest
import aktual.api.model.banksync.SimpleFinBatchResponse
import aktual.budget.model.AccountSyncSource

/**
 * Talks to the sync server's bank sync endpoints, which proxy each provider (GoCardless, SimpleFIN
 * etc.) using credentials held on the server. Nothing here touches the budget: the client decides
 * what to do with downloaded transactions, then sends the result back through the usual budget
 * sync.
 */
interface BankSyncApi {
  /** Whether [source] has credentials set up on the server. */
  suspend fun status(source: AccountSyncSource): BankSyncStatusResponse

  /**
   * Downloads transactions and balances for one linked account from [source]. Gives up after a
   * minute with a [BankSyncTransactionsResponse.ProviderError.TIMED_OUT] error.
   */
  suspend fun transactions(
    source: AccountSyncSource,
    request: BankSyncTransactionsRequest,
  ): BankSyncTransactionsResponse

  /** Downloads several SimpleFIN accounts in one request, giving up after five minutes. */
  suspend fun simpleFinBatch(request: SimpleFinBatchRequest): SimpleFinBatchResponse
}
