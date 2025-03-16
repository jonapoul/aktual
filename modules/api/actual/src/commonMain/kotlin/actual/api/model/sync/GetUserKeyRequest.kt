package actual.api.model.sync

import actual.budget.model.BudgetId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetUserKeyRequest(
  @SerialName("fileId") val id: BudgetId,
)
