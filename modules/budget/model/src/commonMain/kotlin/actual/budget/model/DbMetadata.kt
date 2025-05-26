package actual.budget.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DbMetadata(
  @SerialName("budgetName") val budgetName: String,
  @SerialName("cloudFileId") val cloudFileId: BudgetId,
  @SerialName("groupId") val groupId: String,
  @SerialName("id") val id: String,
  @SerialName("lastScheduleRun") val lastScheduleRun: LocalDate,
  @SerialName("lastSyncedTimestamp") val lastSyncedTimestamp: Timestamp,
  @SerialName("lastUploaded") val lastUploaded: LocalDate,
  @SerialName("resetClock") val resetClock: Boolean,
  @SerialName("userId") val userId: String,
  @SerialName("encryptKeyId") val encryptKeyId: String? = null,
)
