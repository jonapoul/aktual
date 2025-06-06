package actual.budget.di

import actual.budget.model.BudgetId
import actual.budget.model.BudgetScope
import actual.core.files.BudgetFiles
import actual.db.AndroidSqlDriverFactory
import actual.db.buildDatabase
import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class BudgetDatabaseModule {
  @Provides
  @BudgetScope
  fun database(
    id: BudgetIdAlias,
    context: Context,
    files: BudgetFiles,
  ) = buildDatabase(
    factory = AndroidSqlDriverFactory(BudgetId(id), context, files),
  )
}
