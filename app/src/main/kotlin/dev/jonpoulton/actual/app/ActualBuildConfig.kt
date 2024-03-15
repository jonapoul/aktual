package dev.jonpoulton.actual.app

import alakazam.android.core.IBuildConfig
import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.datetime.Instant
import javax.inject.Inject
import dev.jonpoulton.actual.core.res.R as ResR

internal class ActualBuildConfig @Inject constructor(
  @ApplicationContext context: Context,
) : IBuildConfig {
  override val debug = BuildConfig.DEBUG
  override val applicationId = BuildConfig.APPLICATION_ID
  override val versionCode = BuildConfig.VERSION_CODE
  override val versionName = BuildConfig.VERSION_NAME
  override val buildTime: Instant = BuildConfig.BUILD_TIME
  override val gitId = BuildConfig.GIT_HASH
  override val manufacturer: String = Build.MANUFACTURER
  override val model: String = Build.MODEL
  override val os = Build.VERSION.SDK_INT
  override val platform = context.getString(ResR.string.app_name)
  override val repoName = "jonapoul/actual-android"
  override val repoUrl = "https://github.com/$repoName"
}
