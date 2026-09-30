package aktual.di

import aktual.budget.BudgetFiles
import aktual.budget.db.SqlDriverFactory
import aktual.core.model.BudgetServer
import aktual.prefs.AppPreferences
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class RunLevelInitialiserImpl(
  private val preferences: AppPreferences,
  private val files: BudgetFiles,
  private val runLevelController: RunLevelController,
  private val driverFactory: SqlDriverFactory,
) : RunLevelInitialiser {
  override suspend operator fun invoke(appGraph: AppGraph) {
    val graphs = mutableListOf<AktualGraph>(appGraph)
    graphs.addFrom(appGraph)
    runLevelController.init(graphs)
  }

  private suspend fun MutableList<AktualGraph>.addFrom(appGraph: AppGraph) {
    val url = preferences.serverUrl.get() ?: return
    val serverChosenGraph = appGraph.serverChosenGraphFactory.create(url)
    add(serverChosenGraph)

    val token = preferences.token.get() ?: return
    val loggedInGraph = serverChosenGraph.loggedInGraphFactory.create(token)
    add(loggedInGraph)

    val budgetId = preferences.lastOpenedBudgetId.get() ?: return
    files.readMetadata(budgetId)?.let { metadata ->
      // only open the driver once we know the metadata exists, otherwise it would leak
      val driver = driverFactory.create(budgetId)
      val server = BudgetServer.Remote(url, token)
      add(appGraph.budgetGraphFactory.create(budgetId, server, metadata, driver))
    }
  }
}
