package actual.test

import alakazam.kotlin.core.BasicBuildConfig
import kotlinx.datetime.Instant

val TestInstant = Instant.fromEpochMilliseconds(1710786854286L) // Mon Mar 18 2024 18:34:14

val TestBuildConfig = BasicBuildConfig(
  applicationId = "dev.jonpoulton.actual.app",
  buildTime = TestInstant,
  debug = false,
  gitId = "abcd1234",
  manufacturer = "Acme, Inc",
  model = "Doodad",
  os = 34,
  platform = "Acme Doodad",
  repoName = "whatever",
  repoUrl = "https://github.com/jonapoul/actual-android",
  versionCode = 123,
  versionName = "1.2.3",
)
