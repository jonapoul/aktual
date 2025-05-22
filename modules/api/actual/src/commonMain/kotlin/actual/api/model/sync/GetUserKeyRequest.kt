package actual.api.model.sync

import actual.budget.model.BudgetId
import dev.drewhamilton.poko.Poko
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Poko class GetUserKeyRequest(
  @SerialName("fileId") val id: BudgetId,
)
