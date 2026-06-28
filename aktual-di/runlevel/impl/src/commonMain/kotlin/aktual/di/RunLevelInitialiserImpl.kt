package aktual.di

import aktual.budget.db.SqlDriverFactory
import aktual.budget.model.BudgetFiles
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

  private suspend fun MutableList<AktualGraph>.addFrom(graph: AppGraph) {
    val url = preferences.serverUrl.get()
    if (url != null) {
      val serverChosenGraph = graph.serverChosenGraphFactory.create(url)
      add(serverChosenGraph)
      addFrom(serverChosenGraph)
    }
  }

  private suspend fun MutableList<AktualGraph>.addFrom(graph: ServerChosenGraph) {
    val token = preferences.token.get()
    if (token != null) {
      val loggedInGraph = graph.loggedInGraphFactory.create(token)
      add(loggedInGraph)
      addFrom(loggedInGraph)
    }
  }

  private suspend fun MutableList<AktualGraph>.addFrom(graph: LoggedInGraph) {
    val budgetId = preferences.lastOpenedBudgetId.get()
    if (budgetId != null) {
      val driver = driverFactory.create(budgetId)
      files.readMetadata(budgetId)?.let { metadata ->
        val budgetGraph = graph.budgetGraphFactory.create(budgetId, metadata, driver)
        add(budgetGraph)
      }
    }
  }
}
