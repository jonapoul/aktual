package aktual.core.di

import aktual.budget.db.AndroidSqlDriverFactory
import aktual.budget.model.BudgetFiles
import aktual.budget.model.DbMetadata
import aktual.budget.model.cloudFileId
import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(AppScope::class)
class AndroidBudgetGraphBuilder(
  private val context: Context,
  private val files: BudgetFiles,
  private val factory: BudgetGraph.Factory,
) : BudgetGraph.Builder {
  override fun invoke(metadata: DbMetadata): BudgetGraph {
    val driverFactory =
      AndroidSqlDriverFactory(id = metadata.cloudFileId, context = context, budgetFiles = files)

    return factory.create(metadata.cloudFileId, metadata, driverFactory)
  }
}
