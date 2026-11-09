package aktual.core.model

import androidx.compose.runtime.Immutable
import kotlin.time.Instant

@Immutable
data class BuildConfig(
  val isDebug: Boolean,
  val buildType: String,
  val versionCode: Int,
  val versionName: String,
  val gitHash: String,
  val buildTime: Instant,
  val defaultPassword: Password,
  val defaultServerUrl: ServerUrl?,
  val repoName: String = REPO_NAME,
  val repoUrl: String = "https://github.com/${REPO_NAME}",
) {
  val isRelease: Boolean
    get() = buildType == RELEASE_BUILD_TYPE

  // Version name without the build type suffix, to compare against release tags
  val baseVersionName: String
    get() = versionName.removeSuffix("-$buildType")
}

const val RELEASE_BUILD_TYPE = "release"

private const val REPO_NAME = "jonapoul/aktual"
