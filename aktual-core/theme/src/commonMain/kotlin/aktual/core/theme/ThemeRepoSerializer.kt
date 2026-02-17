package aktual.core.theme

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

// E.g. "Juulz/simple-dark"
internal object ThemeRepoSerializer : KSerializer<ThemeRepo> {
  override val descriptor = PrimitiveSerialDescriptor("ThemeRepo", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: ThemeRepo) =
    encoder.encodeString("${value.userName}/${value.repoName}")

  override fun deserialize(decoder: Decoder): ThemeRepo {
    val string = decoder.decodeString()
    val parts = string.split('/')
    require(parts.size == 2) { "Malformed repository ID: $string" }
    return ThemeRepo(userName = parts[0], repoName = parts[1])
  }
}
