package aktual.core.di

import aktual.budget.db.JvmSqlDriverFactory
import aktual.budget.model.BudgetFiles
import aktual.budget.model.DbMetadata
import aktual.budget.model.cloudFileId
import aktual.budget.model.database
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(AppScope::class)
class JvmBudgetGraphBuilder(
  private val files: BudgetFiles,
  private val factory: BudgetGraph.Factory,
) : BudgetGraph.Builder {
  override fun invoke(metadata: DbMetadata): BudgetGraph {
    val dbFile = files.database(metadata.cloudFileId, mkdirs = true).toFile()
    val driverFactory = JvmSqlDriverFactory(dbFile)
    return factory.create(metadata.cloudFileId, metadata, driverFactory)
  }
}
