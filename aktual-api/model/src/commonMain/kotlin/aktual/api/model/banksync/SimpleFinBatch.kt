package aktual.api.model.banksync

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SimpleFinBatchRequest(
  @SerialName("accountId") val accountIds: List<String>,
  @SerialName("startDate") val startDates: List<LocalDate>,
) {
  init {
    require(accountIds.size == startDates.size) { "Need one start date per account" }
  }
}

// downloadSimpleFinTransactions() in packages/loot-core/src/server/accounts/sync.ts
sealed interface SimpleFinBatchResponse {
  data class Success(val accounts: Map<String, BankSyncTransactionsResponse>) :
    SimpleFinBatchResponse

  data class Failed(val error: BankSyncTransactionsResponse.Failure) : SimpleFinBatchResponse
}
