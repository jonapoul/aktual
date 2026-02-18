package aktual.core.theme

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ThemeSummary(
  @SerialName("name") val name: String,
  @SerialName("repo") val repo: ThemeRepo,
  @SerialName("colors") val colors: List<String>,
)
