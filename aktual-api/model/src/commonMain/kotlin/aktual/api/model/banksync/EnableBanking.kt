package aktual.api.model.banksync

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A bank Enable Banking can log in to, which [name] and [country] identify. See EnableBankingAspsp
 * in packages/loot-core/src/types/models/enablebanking.ts.
 *
 * @property maxConsentValidity How long, in seconds, the bank lets a login last.
 */
@Serializable
data class EnableBankingBank(
  @SerialName("name") val name: String,
  @SerialName("country") val country: String,
  @SerialName("logo") val logo: String? = null,
  @SerialName("beta") val isBeta: Boolean = false,
  @SerialName("maximum_consent_validity") val maxConsentValidity: Long? = null,
)

/** Whether the user logs in to their bank as a person or a business. */
enum class EnableBankingAccountType(val value: String) {
  Personal("personal"),
  Business("business"),
}

sealed interface EnableBankingBanksResponse {
  data class Success(val banks: List<EnableBankingBank>) : EnableBankingBanksResponse

  data class Failed(val error: BankSyncTransactionsResponse.Failure) : EnableBankingBanksResponse
}

sealed interface EnableBankingLoginResponse {
  /** The user logs in to their bank at [url], after which [state] lists its accounts. */
  @Serializable
  data class Success(
    @SerialName("url") val url: String,
    @SerialName("state") val state: String,
  ) : EnableBankingLoginResponse

  data class Failed(val error: BankSyncTransactionsResponse.Failure) : EnableBankingLoginResponse
}

sealed interface EnableBankingAccountsResponse {
  /** Each account's [ExternalBankAccount.orgId] is its own ID, to store as the bank's ID. */
  data class Success(val accounts: List<ExternalBankAccount>) : EnableBankingAccountsResponse

  data class Failed(val error: BankSyncTransactionsResponse.Failure) : EnableBankingAccountsResponse
}
