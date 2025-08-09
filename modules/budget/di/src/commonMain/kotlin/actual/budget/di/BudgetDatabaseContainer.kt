package actual.budget.di

import actual.budget.db.BudgetDatabase
import actual.budget.db.SqlDriverFactory
import actual.budget.db.buildDatabase
import actual.budget.model.BudgetScope
import app.cash.sqldelight.db.SqlDriver
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
@ContributesTo(BudgetScope::class)
internal object BudgetDatabaseContainer {
  @Provides
  @SingleIn(BudgetScope::class)
  fun driver(factory: SqlDriverFactory): SqlDriver = factory.create()

  @Provides
  @SingleIn(BudgetScope::class)
  fun database(driver: SqlDriver): BudgetDatabase = buildDatabase(driver)
}
