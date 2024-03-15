package dev.jonpoulton.actual.app

import alakazam.android.core.IBuildConfig
import alakazam.kotlin.core.DefaultDispatcher
import alakazam.kotlin.core.IODispatcher
import alakazam.kotlin.core.MainDispatcher
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
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
