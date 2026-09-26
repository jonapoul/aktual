package aktual.budget.model

import fallback.serializer.Fallback
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class GroupBy {
  @SerialName("Account") Account,
  @SerialName("Category") Category,
  @SerialName("Group") Group,
  @SerialName("Interval") Interval,
  @SerialName("Payee") Payee,
  @Fallback Unknown,
}
