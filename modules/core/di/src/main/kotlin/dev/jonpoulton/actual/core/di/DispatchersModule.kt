package dev.jonpoulton.actual.core.di

import alakazam.kotlin.core.DefaultDispatcher
import alakazam.kotlin.core.IODispatcher
import alakazam.kotlin.core.MainDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

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
