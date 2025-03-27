package actual.about.licenses.data

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

/**
 * From https://github.com/cashapp/licensee/blob/trunk/src/main/kotlin/app/cash/licensee/outputModel.kt
 */
@Serializable
data class ArtifactDetail(
  val groupId: String,
  val artifactId: String,
  val version: String,
  val name: String? = null,
  val spdxLicenses: Set<SpdxLicense> = emptySet(),
  val unknownLicenses: Set<UnknownLicense> = emptySet(),
  val scm: ArtifactScm? = null,
) {
  @get:Stable
  val id: String get() = "$groupId:$artifactId:$version"
}

@Serializable
data class SpdxLicense(
  val identifier: String,
  val name: String,
  val url: String,
)

val Apache2 = SpdxLicense(
  identifier = "Apache-2.0",
  name = "Apache License 2.0",
  url = "https://www.apache.org/licenses/LICENSE-2.0",
)

@Serializable
data class UnknownLicense(
  val name: String?,
  val url: String?,
)

@Serializable
data class ArtifactScm(
  val url: String,
)
