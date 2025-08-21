package actual.app.di

import actual.core.model.BuildConfig
import actual.core.model.Password
import actual.core.model.ServerUrl
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import kotlin.time.Instant
import actual.app.di.BuildConfig as AgpBuildConfig

@BindingContainer
@ContributesTo(AppScope::class)
object AndroidBuildConfigContainer {
  @Provides
  fun buildConfig(): BuildConfig = ANDROID_BUILD_CONFIG
}

private val ANDROID_BUILD_CONFIG = BuildConfig(
  isDebug = AgpBuildConfig.DEBUG,
  versionCode = VERSION_CODE,
  versionName = VERSION_NAME,
  gitHash = GIT_HASH,
  buildTime = Instant.fromEpochMilliseconds(BUILD_TIME_MS),
  defaultPassword = DEFAULT_PASSWORD?.let(::Password) ?: Password.Empty,
  defaultServerUrl = DEFAULT_URL?.let(::ServerUrl),
)
