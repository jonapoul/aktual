package aktual.budget.model

import fallback.serializer.Fallback
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SortBy {
  @SerialName("asc") Asc,
  @SerialName("budget") Budget,
  @SerialName("desc") Desc,
  @SerialName("name") Name,
  @Fallback Unknown,
}
