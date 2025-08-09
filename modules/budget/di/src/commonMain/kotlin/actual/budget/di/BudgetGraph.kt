@file:Suppress("ktlint:standard:parameter-list-wrapping")

package actual.budget.di

import actual.budget.db.BudgetDatabase
import actual.budget.db.SqlDriverFactory
import actual.budget.model.BudgetFiles
import actual.budget.model.BudgetId
import actual.budget.model.BudgetScope
import actual.budget.model.DbMetadata
import actual.budget.prefs.BudgetLocalPreferences
import alakazam.kotlin.core.CoroutineContexts
import app.cash.sqldelight.db.SqlDriver
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.CoroutineScope

@DependencyGraph(BudgetScope::class)
interface BudgetGraph : AutoCloseable {
  val driver: SqlDriver
  val database: BudgetDatabase
  val localPreferences: BudgetLocalPreferences

  val budgetId: BudgetId get() = localPreferences.value.cloudFileId

  override fun close() {
    driver.close()
  }

  @DependencyGraph.Factory
  fun interface Factory {
    fun create(
      @Provides files: BudgetFiles,
      @Provides scope: CoroutineScope,
      @Provides contexts: CoroutineContexts,
      @Provides metadata: DbMetadata,
      @Provides driverFactory: SqlDriverFactory,
    ): BudgetGraph
  }

  // Platform-specific implementation to call the above
  fun interface Builder {
    operator fun invoke(metadata: DbMetadata): BudgetGraph
  }
}

fun BudgetGraph.throwIfWrongBudget(expected: BudgetId) {
  require(budgetId == expected) {
    "Loading from the wrong budget! Expected $expected, got $budgetId"
  }
}
