package actual.api.model.sync

import actual.core.model.LoginToken
import actual.budget.model.BudgetId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetUserKeyRequest(
  @SerialName("fileId") val id: BudgetId,
  @SerialName("token") val token: LoginToken,
)
