package actual.core.di

import actual.core.files.AndroidAssets
import actual.core.files.AndroidDatabaseDirectory
import actual.core.files.Assets
import actual.core.files.DatabaseDirectory
import dagger.Binds
import dagger.Module

@Module
interface FilesModule {
  @Binds fun assets(impl: AndroidAssets): Assets
  @Binds fun databaseDir(impl: AndroidDatabaseDirectory): DatabaseDirectory
}
