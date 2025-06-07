package actual.core.di

import actual.budget.model.AndroidBudgetFiles
import actual.budget.model.BudgetFiles
import actual.core.model.AndroidAssets
import actual.core.model.Assets
import dagger.Binds
import dagger.Module

@Module
interface FilesModule {
  @Binds
  fun assets(impl: AndroidAssets): Assets

  @Binds
  fun databaseDir(impl: AndroidBudgetFiles): BudgetFiles
}
