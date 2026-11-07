package aktual.api.model.banksync

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Body of a provider's `/transactions` request. [accountId] is the provider's ID for the account,
 * i.e. `accounts.account_id`, not Actual's own account ID.
 */
@Serializable
data class BankSyncTransactionsRequest(
  @SerialName("accountId") val accountId: String,
  @SerialName("startDate") val startDate: LocalDate,
  // GoCardless only - `banks.bank_id` of the account's bank
  @SerialName("requisitionId") val requisitionId: String? = null,
  // GoCardless only - upstream only asks for balances on an account's first sync
  @SerialName("includeBalance") val includeBalance: Boolean? = null,
  // Enable Banking only - `banks.name` of the account's bank
  @SerialName("aspspName") val aspspName: String? = null,
)
