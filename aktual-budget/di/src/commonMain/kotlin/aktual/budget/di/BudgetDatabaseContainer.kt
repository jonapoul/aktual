package aktual.budget.di

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.SqlDriverFactory
import aktual.budget.db.buildDatabase
import aktual.budget.model.BudgetScope
import app.cash.sqldelight.db.SqlDriver
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
@ContributesTo(BudgetScope::class)
object BudgetDatabaseContainer {
  @Provides
  @SingleIn(BudgetScope::class)
  fun driver(factory: SqlDriverFactory): SqlDriver = factory.create()

  @Provides
  @SingleIn(BudgetScope::class)
  fun database(driver: SqlDriver): BudgetDatabase = buildDatabase(driver)
}
