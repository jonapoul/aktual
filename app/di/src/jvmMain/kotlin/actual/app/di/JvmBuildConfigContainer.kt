package actual.app.di

import actual.core.model.BuildConfig
import actual.core.model.Password
import actual.core.model.ServerUrl
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import kotlin.time.Instant

@BindingContainer
@ContributesTo(AppScope::class)
object JvmBuildConfigContainer {
  @Provides
  fun buildConfig(): BuildConfig = JVM_BUILD_CONFIG
}

private val JVM_BUILD_CONFIG = BuildConfig(
  isDebug = DEBUG,
  versionCode = VERSION_CODE,
  versionName = VERSION_NAME,
  gitHash = GIT_HASH,
  buildTime = Instant.fromEpochMilliseconds(BUILD_TIME_MS),
  defaultPassword = DEFAULT_PASSWORD?.let(::Password) ?: Password.Empty,
  defaultServerUrl = DEFAULT_URL?.let(::ServerUrl),
)
