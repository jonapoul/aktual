package aktual.core.theme

import alakazam.kotlin.SerializableByString
import alakazam.kotlin.enumStringSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable(ThemeMode.Serializer::class)
enum class ThemeMode(override val value: String) : SerializableByString {
  Light("light"),
  Dark("dark");

  internal object Serializer : KSerializer<ThemeMode> by enumStringSerializer()
}
