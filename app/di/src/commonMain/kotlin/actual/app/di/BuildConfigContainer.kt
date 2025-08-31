package actual.app.di

import actual.core.model.BuildConfig
import actual.core.model.Password
import actual.core.model.ServerUrl
import kotlin.time.Instant

internal fun buildConfig(isDebug: Boolean) = BuildConfig(
  isDebug = isDebug,
  versionCode = VERSION_CODE,
  versionName = VERSION_NAME,
  gitHash = GIT_HASH,
  buildTime = Instant.fromEpochMilliseconds(BUILD_TIME_MS),
  defaultPassword = DEFAULT_PASSWORD?.let(::Password) ?: Password.Empty,
  defaultServerUrl = DEFAULT_URL?.let(::ServerUrl),
)
