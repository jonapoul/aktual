package dev.jonpoulton.actual.app

import alakazam.android.core.IBuildConfig
import android.content.Context
import android.os.Build
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton
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

@Module
@InstallIn(SingletonComponent::class)
internal interface BuildConfigModule {
  @Binds
  @Singleton
  fun buildConfig(impl: ActualBuildConfig): IBuildConfig
}
