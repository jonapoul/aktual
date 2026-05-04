package aktual.di

import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

interface RunLevelState {
  // The VM graph of the current run level, which contains all VM providers for all VMs in parent
  // scopes
  fun viewModelGraph(): Flow<ViewModelGraph>

  // Get a specific run level graph, if it's active
  operator fun <G : AktualGraph> get(type: KClass<G>): G?

  fun all(): Flow<List<AktualGraph>>

  fun app(): Flow<AppGraph>

  fun serverChosen(): Flow<ServerChosenGraph?>

  fun loggedIn(): Flow<LoggedInGraph?>

  fun budget(): Flow<BudgetGraph?>
}
