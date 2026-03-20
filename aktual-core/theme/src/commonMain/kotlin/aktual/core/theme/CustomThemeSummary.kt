package aktual.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class CustomThemeSummary(
  @SerialName("name") val name: String,
  @SerialName("repo") val repo: CustomThemeRepo,
  @SerialName("colors") val colors: List<@Contextual Color>,
  @SerialName("mode") val mode: ThemeMode,
)
