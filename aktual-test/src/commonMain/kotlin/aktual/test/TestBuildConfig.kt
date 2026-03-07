package aktual.test

import aktual.core.model.BuildConfig
import aktual.core.model.Password
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import kotlin.time.Instant

val TestInstant = Instant.fromEpochMilliseconds(1_710_786_854_286L) // Mon Mar 18 2024 18:34:14

val TestBuildConfig =
  BuildConfig(
    buildTime = TestInstant,
    isDebug = false,
    gitHash = "abcd1234",
    versionCode = 123,
    versionName = "1.2.3",
    defaultPassword = Password.Empty,
    defaultServerUrl = null,
  )

@BindingContainer
@ContributesTo(AppScope::class)
object TestBuildConfigContainer {
  @Provides fun buildConfig(): BuildConfig = TestBuildConfig
}
