package aktual.about.data

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * From
 * https://github.com/cashapp/licensee/blob/trunk/src/main/kotlin/app/cash/licensee/outputModel.kt
 */
@Serializable
@Immutable
data class ArtifactDetail(
  val groupId: String,
  val artifactId: String,
  val version: String,
  val name: String? = null,
  val spdxLicenses: Set<SpdxLicense> = emptySet(),
  val unknownLicenses: Set<UnknownLicense> = emptySet(),
  val scm: ArtifactScm? = null,
) {
  val id: String
    get() = "$groupId:$artifactId:$version"
}

@Immutable
@Serializable
data class SpdxLicense(val identifier: String, val name: String, val url: String)

val Apache2 =
  SpdxLicense(
    identifier = "Apache-2.0",
    name = "Apache License 2.0",
    url = "https://www.apache.org/licenses/LICENSE-2.0",
  )

@Immutable @Serializable data class UnknownLicense(val name: String?, val url: String?)

@Immutable @Serializable data class ArtifactScm(val url: String)
