package aktual.budget.model

import fallback.serializer.Fallback
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class GraphType {
  @SerialName("AreaGraph") AreaGraph,
  @SerialName("BarGraph") BarGraph,
  @SerialName("DonutGraph") DonutGraph,
  @SerialName("LineGraph") LineGraph,
  @SerialName("StackedBarGraph") StackedBarGraph,
  @SerialName("TableGraph") TableGraph,
  @Fallback Unknown,
}
