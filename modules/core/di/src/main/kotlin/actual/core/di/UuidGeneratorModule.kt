package actual.core.di

import actual.core.model.RandomUuidGenerator
import actual.core.model.UuidGenerator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface UuidGeneratorModule {
  @Binds
  @Singleton
  fun generator(impl: RandomUuidGenerator): UuidGenerator
}
