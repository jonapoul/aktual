package actual.android.app

import actual.account.model.Password
import actual.url.model.ServerUrl
import android.content.Context
import android.os.Build
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import actual.core.res.R as CoreR

@Suppress("UnnecessarySafeCall", "UnreachableCode")
@Module
@InstallIn(SingletonComponent::class)
internal class BuildConfigModule {
  @Provides
  @Singleton
  fun buildConfig(context: Context) = ActualBuildConfig(
    debug = BuildConfig.DEBUG,
    applicationId = BuildConfig.APPLICATION_ID,
    versionCode = BuildConfig.VERSION_CODE,
    versionName = BuildConfig.VERSION_NAME,
    buildTime = BuildConfig.BUILD_TIME,
    gitId = BuildConfig.GIT_HASH,
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
    BuildConfig.DEFAULT_PASSWORD?.let(::Password) ?: Password.Empty
  }

  @Provides
  @Singleton
  fun url() = ServerUrl.Provider {
    BuildConfig.DEFAULT_URL?.let(::ServerUrl)
  }
}

private const val REPO_NAME = "jonapoul/actual-android"
