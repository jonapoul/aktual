package actual.budget.di

import actual.budget.db.BudgetDatabase
import actual.budget.db.SqlDriverFactory
import actual.budget.db.buildDatabase
import actual.budget.model.BudgetScope
import app.cash.sqldelight.db.SqlDriver
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

@BindingContainer
@ContributesTo(BudgetScope::class)
internal object BudgetDatabaseContainer {
  @Provides
  fun driver(factory: SqlDriverFactory): SqlDriver = factory.create()

  @Provides
  fun database(driver: SqlDriver): BudgetDatabase = buildDatabase(driver)
}
