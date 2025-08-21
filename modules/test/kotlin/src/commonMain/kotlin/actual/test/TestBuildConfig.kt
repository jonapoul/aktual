package actual.test

import actual.core.model.BuildConfig
import actual.core.model.Password
import kotlin.time.Instant

val TestInstant = Instant.fromEpochMilliseconds(1710786854286L) // Mon Mar 18 2024 18:34:14

val TestBuildConfig = BuildConfig(
  buildTime = TestInstant,
  isDebug = false,
  gitHash = "abcd1234",
  versionCode = 123,
  versionName = "1.2.3",
  defaultPassword = Password.Empty,
  defaultServerUrl = null,
)
