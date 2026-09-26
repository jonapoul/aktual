package aktual.core.theme

import fallback.serializer.Fallback
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ThemeMode {
  @SerialName("light") Light,
  @SerialName("dark") Dark,
  @Fallback Unknown,
}
