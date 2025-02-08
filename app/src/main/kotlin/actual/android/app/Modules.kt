package actual.android.app

import actual.about.data.GithubJson
import actual.api.core.buildOkHttp
import actual.api.core.buildRetrofit
import actual.core.coroutines.CoroutineContexts
import actual.core.coroutines.DefaultCoroutineContexts
import actual.licenses.data.AndroidAssetsProvider
import actual.licenses.data.AssetsProvider
import actual.log.Logger
import actual.url.model.ServerUrl
import alakazam.android.core.UrlOpener
import alakazam.kotlin.core.InfiniteLoopController
import alakazam.kotlin.core.LoopController
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.preference.PreferenceManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.jonpoulton.preferences.android.AndroidSharedPreferences
import dev.jonpoulton.preferences.core.Preferences
import github.api.client.GithubApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.datetime.Clock
import retrofit2.create
import javax.inject.Singleton
import actual.core.res.R as CoreR

@Module
@InstallIn(SingletonComponent::class)
internal class ContextModule {
  @Provides
  @Singleton
  fun context(
    @ApplicationContext app: Context,
  ): Context = app
}

@Module
@InstallIn(SingletonComponent::class)
internal class ScopeModule {
  @Provides
  @Singleton
  fun scope(): CoroutineScope = CoroutineScope(SupervisorJob())
}

@Module
@InstallIn(SingletonComponent::class)
internal class LoopControllerModule {
  @Provides
  @Singleton
  fun loopController(): LoopController = InfiniteLoopController
}

@Module
@InstallIn(SingletonComponent::class)
internal interface CoroutineContextsModule {
  @Binds
  @Singleton
  fun contexts(impl: DefaultCoroutineContexts): CoroutineContexts
}

@Module
@InstallIn(SingletonComponent::class)
internal class ClockModule {
  @Provides
  fun clock(): Clock = Clock.System
}

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
}

private const val REPO_NAME = "jonapoul/actual-android"

@Module
@InstallIn(SingletonComponent::class)
internal class PreferencesModule {
  @Provides
  @Singleton
  fun sharedPrefs(context: Context): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

  @Provides
  @Singleton
  fun prefs(
    prefs: SharedPreferences,
    contexts: CoroutineContexts,
  ): Preferences = AndroidSharedPreferences(prefs, contexts.io)
}

@Module
@InstallIn(SingletonComponent::class)
internal interface LoggerModule {
  @Binds
  @Singleton
  fun logger(impl: ActualLogger): Logger
}

@Module
@InstallIn(SingletonComponent::class)
internal interface LicensesModule {
  @Binds
  fun assets(impl: AndroidAssetsProvider): AssetsProvider
}

@Module
@InstallIn(SingletonComponent::class)
internal class AlakazamModule {
  @Provides
  fun urlOpener(context: Context) = UrlOpener(context)
}

@Module
@InstallIn(ViewModelComponent::class)
internal class GithubModule {
  @Provides
  fun api(
    logger: Logger,
    buildConfig: ActualBuildConfig,
  ): GithubApi = buildRetrofit(
    client = buildOkHttp(logger, buildConfig.debug, tag = "GITHUB"),
    url = ServerUrl("https://api.github.com"),
    json = GithubJson,
  ).create()
}
