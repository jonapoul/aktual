package aktual.core.di

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.SqlDriverFactory
import aktual.budget.model.BudgetId
import aktual.budget.model.BudgetScope
import aktual.budget.model.DbMetadata
import aktual.budget.prefs.BudgetLocalPreferences
import app.cash.sqldelight.db.SqlDriver
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides

@GraphExtension(BudgetScope::class)
interface BudgetGraph {
  val driver: SqlDriver
  val database: BudgetDatabase
  val localPreferences: BudgetLocalPreferences
  val budgetId: BudgetId

  @GraphExtension.Factory
  fun interface Factory {
    fun create(
      @Provides budgetId: BudgetId,
      @Provides metadata: DbMetadata,
      @Provides driverFactory: SqlDriverFactory,
    ): BudgetGraph
  }

  // Platform-specific implementation to invoke BudgetGraph.Factory
  fun interface Builder {
    operator fun invoke(metadata: DbMetadata): BudgetGraph
  }

  fun throwIfWrongBudget(expected: BudgetId) {
    require(budgetId == expected) {
      "Loading from the wrong budget! Expected $expected, got $budgetId"
    }
  }
}
