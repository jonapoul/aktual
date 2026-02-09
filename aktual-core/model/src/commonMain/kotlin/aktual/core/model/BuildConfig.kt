package aktual.core.model

import kotlin.time.Instant

data class BuildConfig(
    val isDebug: Boolean,
    val versionCode: Int,
    val versionName: String,
    val gitHash: String,
    val buildTime: Instant,
    val defaultPassword: Password,
    val defaultServerUrl: ServerUrl?,
    val repoName: String = REPO_NAME,
    val repoUrl: String = "https://github.com/${REPO_NAME}",
)

private const val REPO_NAME = "jonapoul/aktual"
