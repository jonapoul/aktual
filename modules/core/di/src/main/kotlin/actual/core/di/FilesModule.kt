package actual.core.di

import actual.core.files.AndroidAssets
import actual.core.files.AndroidFileSystem
import actual.core.files.Assets
import actual.core.files.FileSystem
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface FilesModule {
  @Binds
  fun assets(impl: AndroidAssets): Assets

  @Binds
  fun fileSystem(impl: AndroidFileSystem): FileSystem
}
