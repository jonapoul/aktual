package aktual.api.model.banksync

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A bank GoCardless can log in to. See GoCardlessInstitution in models/gocardless.ts. */
@Serializable
data class GoCardlessBank(
  @SerialName("id") val id: String,
  @SerialName("name") val name: String,
  @SerialName("logo") val logo: String? = null,
)

sealed interface GoCardlessBanksResponse {
  data class Success(val banks: List<GoCardlessBank>) : GoCardlessBanksResponse

  data class Failed(val error: BankSyncTransactionsResponse.Failure) : GoCardlessBanksResponse
}

sealed interface GoCardlessLoginResponse {
  /** The user logs in to their bank at [link], after which [requisitionId] lists its accounts. */
  @Serializable
  data class Success(
    @SerialName("link") val link: String,
    @SerialName("requisitionId") val requisitionId: String,
  ) : GoCardlessLoginResponse

  data class Failed(val error: BankSyncTransactionsResponse.Failure) : GoCardlessLoginResponse
}

sealed interface GoCardlessAccountsResponse {
  /** The user hasn't finished logging in yet. */
  data object Pending : GoCardlessAccountsResponse

  /** Each account's [ExternalBankAccount.orgId] is the requisition, to store as the bank's ID. */
  data class Success(val accounts: List<ExternalBankAccount>) : GoCardlessAccountsResponse

  data class Failed(val error: BankSyncTransactionsResponse.Failure) : GoCardlessAccountsResponse
}
