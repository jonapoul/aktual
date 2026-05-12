package aktual.di

import aktual.budget.model.DbMetadata
import aktual.core.model.ServerUrl
import aktual.core.model.Token

interface RunLevelController : AutoCloseable {
  fun init(graphs: List<AktualGraph>)

  fun onServerChosen(url: ServerUrl): ServerChosenGraph

  fun onLoggedIn(token: Token): LoggedInGraph

  fun onBudget(metadata: DbMetadata): BudgetGraph

  fun onBudgetClosed()

  fun onLoggedOut()

  fun onServerCleared()
}
