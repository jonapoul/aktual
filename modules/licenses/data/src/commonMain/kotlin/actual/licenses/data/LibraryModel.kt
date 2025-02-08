package actual.licenses.data

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class LibraryModel(
  @SerialName(value = "project") val project: String,
  @SerialName(value = "description") val description: String?,
  @SerialName(value = "version") val version: String,
  @SerialName(value = "developers") val developers: List<String>,
  @SerialName(value = "url") val url: String?,
  @SerialName(value = "year") val year: Int?,
  @SerialName(value = "licenses") val licenses: List<LicenseModel>,
  @SerialName(value = "dependency") val dependency: String,
) {
  fun cleanedUp(): LibraryModel = copy(
    licenses = licenses.map { it.cleanUp() },
    developers = developers.map { if (it.matches(AOSP_REGEX)) AOSP else it },
    dependency = dependency
      .split(":")
      .subList(fromIndex = 0, toIndex = 2)
      .joinToString(separator = ":"),
  )

  companion object {
    private val AOSP_REGEX = ".*?Android Open Source Project".toRegex()
    private const val AOSP = "AOSP"
  }
}
