package aktual.core.theme

import aktual.core.model.ThemeId
import androidx.compose.runtime.Immutable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Immutable
@Serializable(CustomThemeRepoSerializer::class)
data class CustomThemeRepo(val userName: String, val repoName: String) :
  Comparable<CustomThemeRepo> {
  override fun toString(): String = "$userName/$repoName"

  override fun compareTo(other: CustomThemeRepo): Int = toString().compareTo(other.toString())
}

fun CustomThemeRepo(string: String): CustomThemeRepo {
  val parts = string.split('/')
  require(parts.size == 2) { "Malformed repository ID: '$string'" }
  return CustomThemeRepo(userName = parts[0], repoName = parts[1])
}

fun CustomThemeRepo.toId(): ThemeId = ThemeId(toString())

// E.g. "Juulz/simple-dark"
internal object CustomThemeRepoSerializer : KSerializer<CustomThemeRepo> {
  override val descriptor = PrimitiveSerialDescriptor("ThemeRepo", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: CustomThemeRepo) =
    encoder.encodeString(value.toString())

  override fun deserialize(decoder: Decoder): CustomThemeRepo =
    CustomThemeRepo(decoder.decodeString())
}
