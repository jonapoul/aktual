package aktual.app.di

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.buildDatabase
import aktual.budget.db.migrateDatabase
import aktual.di.BudgetScope
import app.cash.sqldelight.db.SqlDriver
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.runBlocking

@BindingContainer
@ContributesTo(BudgetScope::class)
object BudgetDatabaseContainer {
  @Provides
  @SingleIn(BudgetScope::class)
  fun database(driver: SqlDriver): BudgetDatabase {
    val db = buildDatabase(driver)

    // TODO: Move this into a runlevel, not runBlocking?
    runBlocking { migrateDatabase(driver, db) }

    return db
  }
}
