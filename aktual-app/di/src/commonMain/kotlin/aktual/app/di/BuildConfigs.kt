package aktual.app.di

import aktual.core.model.BuildConfig
import aktual.core.model.Password
import aktual.core.model.RELEASE_BUILD_TYPE
import aktual.core.model.ServerUrl
import kotlin.time.Instant

@Suppress("UNNECESSARY_SAFE_CALL")
fun buildConfig(isDebug: Boolean, buildType: String): BuildConfig {
  val type = buildType.lowercase()
  return BuildConfig(
    isDebug = isDebug,
    buildType = type,
    versionCode = VERSION_CODE,
    versionName = if (type == RELEASE_BUILD_TYPE) VERSION_NAME else "$VERSION_NAME-$type",
    gitHash = GIT_HASH.substring(startIndex = 0, endIndex = 8),
    buildTime = Instant.fromEpochMilliseconds(BUILD_TIME_MS),
    defaultPassword = DEFAULT_PASSWORD?.let(::Password) ?: Empty,
    defaultServerUrl = DEFAULT_URL?.let(::ServerUrl),
  )
}
