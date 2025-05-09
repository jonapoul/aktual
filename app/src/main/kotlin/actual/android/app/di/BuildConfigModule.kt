package actual.android.app.di

import actual.core.res.R
import alakazam.kotlin.core.BasicBuildConfig
import android.content.Context
import android.os.Build
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import actual.android.app.BuildConfig as AppBuildConfig
import alakazam.kotlin.core.BuildConfig as AlakazamBuildConfig

@Module
@InstallIn(SingletonComponent::class)
internal class BuildConfigModule {
  @Provides
  @Singleton
  fun buildConfig(context: Context): AlakazamBuildConfig = BasicBuildConfig(
    debug = AppBuildConfig.DEBUG,
    applicationId = AppBuildConfig.APPLICATION_ID,
    versionCode = AppBuildConfig.VERSION_CODE,
    versionName = AppBuildConfig.VERSION_NAME,
    buildTime = AppBuildConfig.BUILD_TIME,
    gitId = AppBuildConfig.GIT_HASH,
    manufacturer = Build.MANUFACTURER,
    model = Build.MODEL,
    os = Build.VERSION.SDK_INT,
    platform = context.getString(R.string.app_name),
    repoName = REPO_NAME,
    repoUrl = "https://github.com/${REPO_NAME}",
  )
}

private const val REPO_NAME = "jonapoul/actual-android"
