package actual.budget.di

import actual.budget.db.AndroidSqlDriverFactory
import actual.budget.db.buildDatabase
import actual.budget.model.BudgetFiles
import actual.budget.model.BudgetScoped
import actual.budget.model.DbMetadata
import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class BudgetDatabaseModule {
  @Provides
  @BudgetScoped
  fun database(
    metadata: DbMetadata,
    context: Context,
    files: BudgetFiles,
  ) = buildDatabase(
    factory = AndroidSqlDriverFactory(metadata.cloudFileId, context, files),
  )
}
