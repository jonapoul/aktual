package aktual.di

import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

interface RunLevelState {
  // The VM factory of the current run level, which contains all VM providers for all VMs in parent
  // scopes
  fun viewModelFactory(): Flow<MetroViewModelFactory>

  // Get a specific run level graph, if it's active
  operator fun <G : AktualGraph> get(type: KClass<G>): G?

  fun all(): Flow<List<AktualGraph>>

  fun app(): Flow<AppGraph>

  fun serverChosen(): Flow<ServerChosenGraph?>

  fun loggedIn(): Flow<LoggedInGraph?>

  fun budget(): Flow<BudgetGraph?>
}
