package actual.core.di

import dagger.Module
import dagger.Provides
import okio.FileSystem
import javax.inject.Singleton

@Module
class FileSystemModule {
  @Provides
  @Singleton
  fun fileSystem(): FileSystem = FileSystem.Companion.SYSTEM
}
