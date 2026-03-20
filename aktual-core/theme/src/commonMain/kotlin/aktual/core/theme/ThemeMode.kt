package aktual.core.theme

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ThemeMode {
  @SerialName("light") Light,
  @SerialName("dark") Dark,
}
