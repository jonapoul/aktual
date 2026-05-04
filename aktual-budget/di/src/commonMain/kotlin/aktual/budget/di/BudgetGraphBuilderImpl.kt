package aktual.budget.di

import aktual.budget.db.SqlDriverFactory
import aktual.budget.model.DbMetadata
import aktual.budget.model.cloudFileId
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class BudgetGraphBuilderImpl(
  private val graphFactory: BudgetGraph.Factory,
  private val driverFactory: SqlDriverFactory,
) : BudgetGraph.Builder {
  override fun invoke(metadata: DbMetadata): BudgetGraph =
    graphFactory.create(
      budgetId = metadata.cloudFileId,
      metadata = metadata,
      driver = driverFactory.create(metadata.cloudFileId),
    )
}
