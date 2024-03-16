package dev.jonpoulton.actual.app

import alakazam.android.core.IBuildConfig
import alakazam.kotlin.core.DefaultDispatcher
import alakazam.kotlin.core.IODispatcher
import alakazam.kotlin.core.MainDispatcher
import android.content.Context
import android.content.SharedPreferences
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.jonpoulton.actual.api.client.buildOkHttp
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface BuildConfigModule {
  @Binds
  @Singleton
  fun buildConfig(impl: ActualBuildConfig): IBuildConfig
}

@Module
@InstallIn(SingletonComponent::class)
internal class ClockModule {
  @Provides
  fun clock(): Clock = Clock.System
}

@Module
@InstallIn(SingletonComponent::class)
internal class OkHttpModule {
  @Provides
  @Singleton
  fun client(): OkHttpClient = buildOkHttp()
}

@Module
@InstallIn(SingletonComponent::class)
internal class PreferencesModule {
  @Provides
  @Singleton
  fun sharedPrefs(@ApplicationContext context: Context): SharedPreferences =
    context.getSharedPreferences("actual", Context.MODE_PRIVATE)

  @Provides
  @Singleton
  fun flowPrefs(prefs: SharedPreferences, io: IODispatcher): FlowSharedPreferences =
    FlowSharedPreferences(prefs, io)
}

@Module
@Suppress("InjectDispatcher")
@InstallIn(SingletonComponent::class)
internal class DispatchersModule {
  @Provides
  @Singleton
  fun ioDispatcher(): IODispatcher = IODispatcher(Dispatchers.IO)

  @Provides
  @Singleton
  fun defaultDispatcher(): DefaultDispatcher = DefaultDispatcher(Dispatchers.Default)

  @Provides
  @Singleton
  fun mainDispatcher(): MainDispatcher = MainDispatcher(Dispatchers.Main)
}
