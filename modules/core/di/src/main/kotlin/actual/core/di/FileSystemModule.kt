package actual.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okio.FileSystem
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class FileSystemModule {
  @Provides
  @Singleton
  fun fileSystem(): FileSystem = FileSystem.Companion.SYSTEM
}
