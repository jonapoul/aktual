package aktual.api.client

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
}
