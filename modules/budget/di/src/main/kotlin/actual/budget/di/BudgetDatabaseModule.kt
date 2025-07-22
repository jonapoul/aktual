package actual.budget.di

import actual.budget.db.AndroidSqlDriverFactory
import actual.budget.db.SqlDriverFactory
import actual.budget.db.buildDatabase
import actual.budget.model.BudgetFiles
import actual.budget.model.BudgetScoped
import actual.budget.model.DbMetadata
import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import dagger.Module
import dagger.Provides

@Module
class BudgetDatabaseModule {
  @Provides
  @BudgetScoped
  fun factory(
    metadata: DbMetadata,
    context: Context,
    files: BudgetFiles,
  ): SqlDriverFactory = AndroidSqlDriverFactory(metadata.cloudFileId, context, files)

  @Provides
  @BudgetScoped
  fun driver(
    factory: SqlDriverFactory,
  ): SqlDriver = factory.create()

  @Provides
  @BudgetScoped
  fun database(
    driver: SqlDriver,
  ) = buildDatabase(driver)
}
