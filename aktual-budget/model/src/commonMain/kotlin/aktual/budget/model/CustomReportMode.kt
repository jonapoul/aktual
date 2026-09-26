package aktual.budget.model

import fallback.serializer.Fallback
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CustomReportMode {
  @SerialName("total") Total,
  @SerialName("time") Time,
  @Fallback Unknown,
}
