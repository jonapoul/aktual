package aktual.di

import aktual.budget.model.BudgetId
import aktual.budget.model.BudgetSyncController
import aktual.budget.model.DbMetadata
import aktual.budget.prefs.BudgetLocalPreferences
import app.cash.sqldelight.db.SqlDriver
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides

@GraphExtension(BudgetScope::class)
interface BudgetGraph : AktualGraph {
  val id: BudgetId
  val syncController: BudgetSyncController
  val localPreferences: BudgetLocalPreferences

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
  @Provides @IntoSet fun binds(driver: SqlDriver): Closeable = Closeable { driver.close() }
}
