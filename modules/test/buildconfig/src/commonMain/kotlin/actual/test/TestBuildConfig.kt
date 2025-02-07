package actual.test

import actual.core.config.BuildConfig
import kotlinx.datetime.Instant

val TestBuildConfig = BuildConfig(
  applicationId = "dev.jonpoulton.actual.app",
  buildTime = Instant.fromEpochMilliseconds(1710786854286L), // Mon Mar 18 2024 18:34:14
  debug = false,
  gitId = "abcd1234",
  manufacturer = "Acme, Inc",
  model = "Doodad",
  os = 34,
  platform = "Acme Doodad",
  repoName = "whatever",
  repoUrl = "github.com/someone/whatever",
  versionCode = 123,
  versionName = "1.2.3",
)
