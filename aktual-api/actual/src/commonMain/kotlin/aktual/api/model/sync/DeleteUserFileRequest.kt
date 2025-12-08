package aktual.api.model.sync

import aktual.budget.model.BudgetId
import aktual.core.model.LoginToken
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeleteUserFileRequest(
  @SerialName("fileId") val id: BudgetId,
  @SerialName("token") val token: LoginToken,
)
