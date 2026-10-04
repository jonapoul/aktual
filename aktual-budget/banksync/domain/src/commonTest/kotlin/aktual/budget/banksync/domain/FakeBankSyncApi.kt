package aktual.budget.banksync.domain

import aktual.api.client.BankSyncApi
import aktual.api.model.banksync.BankSyncStatusResponse
import aktual.api.model.banksync.BankSyncTransaction
import aktual.api.model.banksync.BankSyncTransactionsRequest
import aktual.api.model.banksync.BankSyncTransactionsResponse
import aktual.api.model.banksync.SimpleFinBatchRequest
import aktual.api.model.banksync.SimpleFinBatchResponse
import aktual.budget.model.AccountSyncSource
import kotlinx.serialization.json.JsonObject

// Answers each account by its provider ID, from [responses], or throws [error] if set
internal class FakeBankSyncApi : BankSyncApi {
  val requests = mutableListOf<Pair<AccountSyncSource, BankSyncTransactionsRequest>>()
  val batchRequests = mutableListOf<SimpleFinBatchRequest>()
  val responses = mutableMapOf<String, BankSyncTransactionsResponse>()
  var batchResponse: SimpleFinBatchResponse? = null
  var error: Exception? = null

  override suspend fun status(source: AccountSyncSource): BankSyncStatusResponse =
    throw UnsupportedOperationException()

  override suspend fun transactions(
    source: AccountSyncSource,
    request: BankSyncTransactionsRequest,
  ): BankSyncTransactionsResponse {
    requests += source to request
    error?.let { throw it }
    return responses.getValue(request.accountId)
  }

  override suspend fun simpleFinBatch(request: SimpleFinBatchRequest): SimpleFinBatchResponse {
    batchRequests += request
    error?.let { throw it }
    return checkNotNull(batchResponse)
  }
}

internal fun success(vararg transactions: JsonObject, balance: Long? = null) =
  BankSyncTransactionsResponse.Success(
    transactions =
      BankSyncTransactionsResponse.Success.Transactions(
        all = transactions.map(::BankSyncTransaction)
      ),
    startingBalance = balance,
  )

internal fun providerError(category: String, code: String = category) =
  BankSyncTransactionsResponse.ProviderError(errorType = category, errorCode = code)
