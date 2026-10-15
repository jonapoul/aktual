package aktual.core.theme

import androidx.compose.runtime.Immutable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Immutable
@Serializable(CustomThemeRepoSerializer::class)
data class CustomThemeRepo(
  val userName: String,
  val repoName: String,
) : Comparable<CustomThemeRepo> {
  val id = "$userName/$repoName"

  // Themes copied and edited in-app, rather than downloaded from GitHub
  val isLocal: Boolean
    get() = userName == LOCAL_USER_NAME

  override fun toString(): String = id

  override fun compareTo(other: CustomThemeRepo): Int = toString().compareTo(other.toString())

  companion object {
    // Not a valid GitHub username, so can't clash with a downloaded theme
    const val LOCAL_USER_NAME = "@local"

    fun local(repoName: String) = CustomThemeRepo(LOCAL_USER_NAME, repoName)
  }
}

fun CustomThemeRepo(string: String): CustomThemeRepo {
  val parts = string.split('/')
  require(parts.size == 2) { "Malformed repository ID: '$string'" }
  return CustomThemeRepo(userName = parts[0], repoName = parts[1])
}

// E.g. "Juulz/simple-dark"
internal object CustomThemeRepoSerializer : KSerializer<CustomThemeRepo> {
  override val descriptor = PrimitiveSerialDescriptor("ThemeRepo", STRING)

  override fun serialize(encoder: Encoder, value: CustomThemeRepo) =
    encoder.encodeString(value.toString())

  override fun deserialize(decoder: Decoder): CustomThemeRepo =
    CustomThemeRepo(decoder.decodeString())
}
