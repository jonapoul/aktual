package aktual.di

import aktual.budget.BudgetLocalPreferences
import aktual.budget.BudgetSyncController
import aktual.budget.model.BudgetId
import aktual.budget.model.DbMetadata
import app.cash.sqldelight.db.SqlDriver
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.ForScope
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Multibinds
import dev.zacsweers.metro.Provides

@GraphExtension(BudgetScope::class)
interface BudgetGraph : AktualGraph {
  val id: BudgetId
  val syncController: BudgetSyncController
  val localPreferences: BudgetLocalPreferences

  override val coroutineScope: BudgetCoroutineScope

  @Multibinds(allowEmpty = true) @ForScope(BudgetScope::class) val budgetCloseables: Set<Closeable>

  @Multibinds(allowEmpty = true)
  @ForScope(BudgetScope::class)
  val budgetInitializables: Set<Initializable>

  override val closeables: Set<Closeable>
    get() = budgetCloseables

  override val initializables: Set<Initializable>
    get() = budgetInitializables

  @GraphExtension.Factory
  @ContributesTo(LoggedInScope::class)
  fun interface Factory {
    fun create(
      @Provides id: BudgetId,
      @Provides metadata: DbMetadata,
      @Provides driver: SqlDriver,
    ): BudgetGraph
  }
}

@BindingContainer
@ContributesTo(BudgetScope::class)
object BudgetScopeBindings {
  @Provides
  @IntoSet
  @ForScope(BudgetScope::class)
  fun binds(driver: SqlDriver): Closeable = Closeable { driver.close() }
}
