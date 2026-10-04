package aktual.api.model.banksync

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Body of SimpleFIN's `/transactions` request for several accounts at once, which SimpleFIN
 * downloads in one go. Each of [accountIds] is `accounts.account_id`, paired with the start date at
 * the same index.
 */
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
  /**
   * Each account's download or error, keyed by `accounts.account_id`. Accounts SimpleFIN didn't
   * send anything for are left out.
   */
  data class Success(val accounts: Map<String, BankSyncTransactionsResponse>) :
    SimpleFinBatchResponse

  /** The whole request failed. */
  data class Failed(val error: BankSyncTransactionsResponse.Failure) : SimpleFinBatchResponse
}
