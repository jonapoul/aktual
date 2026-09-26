package aktual.budget.model

import fallback.serializer.Fallback
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class BalanceType {
  @SerialName("Deposit") Deposit,
  @SerialName("Expense") Expense,
  @SerialName("Net") Net,
  @SerialName("Net Deposit") NetDeposit,
  @SerialName("Net Payment") NetPayment,
  @SerialName("Payment") Payment,
  @Fallback Unknown,
}
