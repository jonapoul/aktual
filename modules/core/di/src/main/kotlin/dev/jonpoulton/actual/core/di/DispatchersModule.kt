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
@InstallIn(SingletonComponent::class)
internal class DispatchersModule {
  @Provides
  @Singleton
  fun io(): IODispatcher = IODispatcher(Dispatchers.IO)

  @Provides
  @Singleton
  fun default(): DefaultDispatcher = DefaultDispatcher(Dispatchers.Default)

  @Provides
  @Singleton
  fun main(): MainDispatcher = MainDispatcher(Dispatchers.Main)
}
