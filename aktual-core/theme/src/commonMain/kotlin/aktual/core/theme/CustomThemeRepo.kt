package aktual.core.theme

import androidx.compose.runtime.Immutable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Immutable
@Serializable(CustomThemeRepoSerializer::class)
data class CustomThemeRepo(val userName: String, val repoName: String)

fun CustomThemeRepo.toId(): Theme.Id = Theme.Id("$userName/$repoName")

// E.g. "Juulz/simple-dark"
internal object CustomThemeRepoSerializer : KSerializer<CustomThemeRepo> {
  override val descriptor = PrimitiveSerialDescriptor("ThemeRepo", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: CustomThemeRepo) =
    with(value) { encoder.encodeString("$userName/$repoName") }

  override fun deserialize(decoder: Decoder): CustomThemeRepo {
    val string = decoder.decodeString()
    val parts = string.split('/')
    require(parts.size == 2) { "Malformed repository ID: $string" }
    return CustomThemeRepo(userName = parts[0], repoName = parts[1])
  }
}
