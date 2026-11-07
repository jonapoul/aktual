package aktual.api.model.sync

import aktual.budget.model.BudgetId
import aktual.core.model.Token
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserFileNameRequest(
  @SerialName("fileId") val id: BudgetId,
  @SerialName("name") val name: String,
  @SerialName("token") val token: Token,
)
