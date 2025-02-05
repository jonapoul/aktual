package actual.test

import alakazam.android.core.IBuildConfig
import kotlinx.datetime.Instant

object TestBuildConfig : IBuildConfig {
  override val applicationId = "dev.jonpoulton.actual.app"
  override val buildTime = Instant.fromEpochMilliseconds(1710786854286L) // Mon Mar 18 2024 18:34:14
  override val debug = false
  override val gitId = "abcd1234"
  override val manufacturer = "Acme, Inc"
  override val model = "Doodad"
  override val os = 34
  override val platform = "Acme Doodad"
  override val repoName = "whatever"
  override val repoUrl = "github.com/someone/whatever"
  override val versionCode = 123
  override val versionName = "1.2.3"
}
