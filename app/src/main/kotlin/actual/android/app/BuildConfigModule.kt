package actual.android.app

import actual.account.model.Password
import actual.url.model.ServerUrl
import alakazam.kotlin.core.BasicBuildConfig
import android.content.Context
import android.os.Build
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import actual.android.app.BuildConfig as ActualBuildConfig
import actual.core.res.R as CoreR
import alakazam.kotlin.core.BuildConfig as AlakazamBuildConfig

@Suppress("USELESS_ELVIS", "UNNECESSARY_SAFE_CALL")
@Module
@InstallIn(SingletonComponent::class)
internal class BuildConfigModule {
  @Provides
  @Singleton
  fun buildConfig(context: Context): AlakazamBuildConfig = BasicBuildConfig(
    debug = ActualBuildConfig.DEBUG,
    applicationId = ActualBuildConfig.APPLICATION_ID,
    versionCode = ActualBuildConfig.VERSION_CODE,
    versionName = ActualBuildConfig.VERSION_NAME,
    buildTime = ActualBuildConfig.BUILD_TIME,
    gitId = ActualBuildConfig.GIT_HASH,
    manufacturer = Build.MANUFACTURER,
    model = Build.MODEL,
    os = Build.VERSION.SDK_INT,
    platform = context.getString(CoreR.string.app_name),
    repoName = REPO_NAME,
    repoUrl = "https://github.com/$REPO_NAME",
  )

  @Provides
  @Singleton
  fun password() = Password.Provider {
    ActualBuildConfig.DEFAULT_PASSWORD?.let(::Password) ?: Password.Empty
  }

  @Provides
  @Singleton
  fun url() = ServerUrl.Provider {
    ActualBuildConfig.DEFAULT_URL?.let(::ServerUrl)
  }
}

private const val REPO_NAME = "jonapoul/actual-android"
