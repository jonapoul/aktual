package aktual.core.theme

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CustomThemeSummary(
  @SerialName("name") val name: String,
  @SerialName("repo") val repo: CustomThemeRepo,
  @SerialName("colors") val colors: List<@Contextual Color>,
)
