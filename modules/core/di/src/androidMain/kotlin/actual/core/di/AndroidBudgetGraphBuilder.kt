package actual.core.di

import actual.budget.db.AndroidSqlDriverFactory
import actual.budget.model.BudgetFiles
import actual.budget.model.DbMetadata
import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(AppScope::class)
class AndroidBudgetGraphBuilder(
  private val context: Context,
  private val files: BudgetFiles,
  private val appGraphHolder: AppGraph.Holder,
) : BudgetGraph.Builder {
  override fun invoke(metadata: DbMetadata): BudgetGraph {
    val driverFactory = AndroidSqlDriverFactory(
      id = metadata.cloudFileId,
      context = context,
      budgetFiles = files,
    )

    return appGraphHolder().create(metadata.cloudFileId, metadata, driverFactory)
  }
}
