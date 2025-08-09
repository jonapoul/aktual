package actual.budget.di

import actual.budget.db.AndroidSqlDriverFactory
import actual.budget.model.BudgetFiles
import actual.budget.model.DbMetadata
import alakazam.kotlin.core.CoroutineContexts
import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.createGraphFactory
import kotlinx.coroutines.CoroutineScope

@Inject
@ContributesBinding(AppScope::class)
class AndroidBudgetGraphBuilder(
  private val context: Context,
  private val scope: CoroutineScope,
  private val contexts: CoroutineContexts,
  private val files: BudgetFiles,
) : BudgetGraph.Builder {
  override fun invoke(metadata: DbMetadata): BudgetGraph {
    val driverFactory = AndroidSqlDriverFactory(
      id = metadata.cloudFileId,
      context = context,
      budgetFiles = files,
    )

    val graphFactory = createGraphFactory<BudgetGraph.Factory>()
    return graphFactory.create(files, scope, contexts, metadata, driverFactory)
  }
}
