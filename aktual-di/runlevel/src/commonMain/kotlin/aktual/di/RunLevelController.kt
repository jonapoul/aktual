package aktual.di

import aktual.budget.model.BudgetId
import aktual.budget.model.DbMetadata
import aktual.core.model.ServerUrl
import aktual.core.model.Token

interface RunLevelController : AutoCloseable {
  fun init(graphs: List<AktualGraph>)

  fun onServerChosen(url: ServerUrl): ServerChosenGraph

  fun onLoggedIn(token: Token): LoggedInGraph

  /** Opens a budget synced with the logged-in server */
  fun onBudget(id: BudgetId, metadata: DbMetadata): BudgetGraph

  /** Opens a budget with no server, closing any server/login levels first */
  fun onOfflineBudget(id: BudgetId, metadata: DbMetadata): BudgetGraph

  fun onBudgetClosed()

  fun onLoggedOut()

  fun onServerCleared()
}
