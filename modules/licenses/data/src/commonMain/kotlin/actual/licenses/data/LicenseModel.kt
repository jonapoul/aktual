package actual.licenses.data

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class LicenseModel(
  @SerialName(value = "license") val license: String,
  @SerialName(value = "license_url") val licenseUrl: String,
) {
  fun cleanUp(): LicenseModel {
    LICENSES_MAP.forEach { (regex, cleaned) -> if (license.contains(regex)) return cleaned }
    return this
  }

  companion object {
    val Apache2 = LicenseModel(license = "Apache 2.0", licenseUrl = "https://www.apache.org/licenses/LICENSE-2.0.txt")
    val MIT = LicenseModel(license = "MIT", licenseUrl = "https://api.github.com/licenses/mit")

    private val LICENSES_MAP = mapOf(
      "Apache.*?2\\.0".toRegex() to Apache2,
      "MIT".toRegex() to MIT,
    )
  }
}
