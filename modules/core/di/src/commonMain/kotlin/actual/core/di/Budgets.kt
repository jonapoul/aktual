package actual.core.di

import actual.budget.db.BudgetDatabase
import actual.budget.db.SqlDriverFactory
import actual.budget.db.buildDatabase
import actual.budget.model.BudgetId
import actual.budget.model.DbMetadata
import actual.budget.prefs.BudgetLocalPreferences
import alakazam.kotlin.core.StateHolder
import app.cash.sqldelight.db.SqlDriver
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.update

sealed interface BudgetScope

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

@Inject
@SingleIn(AppScope::class)
class BudgetGraphHolder(
  private val budgetGraphBuilder: BudgetGraph.Builder,
) : StateHolder<BudgetGraph?>(initialState = null), AutoCloseable {
  fun require(): BudgetGraph = requireNotNull(value) { "No budget graph loaded!" }

  fun clear() = update { null }

  fun update(metadata: DbMetadata): BudgetGraph {
    val budgetGraph = budgetGraphBuilder(metadata)
    update { budgetGraph }
    return budgetGraph
  }

  override fun close() {
    value?.driver?.close()
  }

  override fun compareAndSet(expect: BudgetGraph?, update: BudgetGraph?): Boolean {
    val previous = value
    val successfullySet = super.compareAndSet(expect, update)
    if (successfullySet) previous?.driver?.close()
    return successfullySet
  }
}

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
