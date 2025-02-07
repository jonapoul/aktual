package actual.android.app

import actual.core.coroutines.CoroutineContexts
import actual.core.coroutines.DefaultCoroutineContexts
import actual.log.Logger
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
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.jonpoulton.preferences.android.AndroidSharedPreferences
import dev.jonpoulton.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.datetime.Clock
import javax.inject.Singleton
import actual.core.res.R as CoreR

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
  fun buildConfig(
    @ApplicationContext context: Context,
  ) = ActualBuildConfig(
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
  fun sharedPrefs(
    @ApplicationContext context: Context,
  ): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

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
