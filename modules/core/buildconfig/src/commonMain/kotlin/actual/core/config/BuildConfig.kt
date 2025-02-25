package actual.core.config

import kotlinx.datetime.Instant

data class BuildConfig(
  val debug: Boolean, // BuildConfig.DEBUG
  val versionName: String, // BuildConfig.VERSION_NAME
  val versionCode: Int, // BuildConfig.VERSION_CODE
  val applicationId: String, // BuildConfig.APPLICATION_ID
  val gitId: String, // custom build config field
  val buildTime: Instant, // custom build config field
  val platform: String, // name of your app
  val os: Int, // Build.VERSION.SDK_INT
  val manufacturer: String, // Build.MANUFACTURER
  val model: String, // Build.MODEL
  val repoName: String, // e.g. "jonapoul/actual-android"
  val repoUrl: String, // e.g. "https://github.com/jonapoul/actual-android"
)
