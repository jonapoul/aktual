package actual.core.di

import actual.budget.db.JvmSqlDriverFactory
import actual.budget.model.BudgetFiles
import actual.budget.model.DbMetadata
import actual.budget.model.database
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(AppScope::class)
class JvmBudgetGraphBuilder(
  private val files: BudgetFiles,
  private val appGraphHolder: AppGraph.Holder,
) : BudgetGraph.Builder {
  override fun invoke(metadata: DbMetadata): BudgetGraph {
    val dbFile = files.database(metadata.cloudFileId, mkdirs = true).toFile()
    val driverFactory = JvmSqlDriverFactory(dbFile)
    return appGraphHolder.get().create(metadata.cloudFileId, metadata, driverFactory)
  }
}
