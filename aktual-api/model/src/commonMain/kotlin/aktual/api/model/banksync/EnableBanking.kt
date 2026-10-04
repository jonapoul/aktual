package aktual.api.model.banksync

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EnableBankingBank(
  @SerialName("name") val name: String,
  @SerialName("country") val country: String,
  @SerialName("logo") val logo: String? = null,
  @SerialName("beta") val isBeta: Boolean = false,
  @SerialName("maximum_consent_validity") val maxConsentValidity: Long? = null,
)

enum class EnableBankingAccountType(val value: String) {
  Personal("personal"),
  Business("business"),
}

sealed interface EnableBankingBanksResponse {
  data class Success(val banks: List<EnableBankingBank>) : EnableBankingBanksResponse

  data class Failed(val error: BankSyncTransactionsResponse.Failure) : EnableBankingBanksResponse
}

sealed interface EnableBankingLoginResponse {
  @Serializable
  data class Success(
    @SerialName("url") val url: String,
    @SerialName("state") val state: String,
  ) : EnableBankingLoginResponse

  data class Failed(val error: BankSyncTransactionsResponse.Failure) : EnableBankingLoginResponse
}

sealed interface EnableBankingAccountsResponse {
  data class Success(val accounts: List<ExternalBankAccount>) : EnableBankingAccountsResponse

  data class Failed(val error: BankSyncTransactionsResponse.Failure) : EnableBankingAccountsResponse
}
