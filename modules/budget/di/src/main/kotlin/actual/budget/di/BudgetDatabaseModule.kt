package actual.budget.di

import actual.budget.db.AndroidSqlDriverFactory
import actual.budget.db.buildDatabase
import actual.budget.model.BudgetFiles
import actual.budget.model.BudgetScope
import actual.budget.model.DbMetadata
import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class BudgetDatabaseModule {
  @Provides
  @BudgetScope
  fun database(
    metadata: DbMetadata,
    context: Context,
    files: BudgetFiles,
  ) = buildDatabase(
    factory = AndroidSqlDriverFactory(metadata.cloudFileId, context, files),
  )
}
