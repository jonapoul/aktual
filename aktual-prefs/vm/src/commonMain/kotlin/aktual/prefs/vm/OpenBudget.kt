package aktual.prefs.vm

import aktual.budget.model.DbMetadataKey.Companion.BudgetName
import aktual.di.AppScope
import aktual.di.RunLevelState
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

// The open budget's name, or null when none is open. An open budget without a name gives ""
fun interface OpenBudgetName {
  fun observe(): Flow<String?>
}

@Inject
@ContributesBinding(AppScope::class)
class OpenBudgetNameImpl internal constructor(private val runLevelState: RunLevelState) :
  OpenBudgetName {
  override fun observe(): Flow<String?> =
    runLevelState
      .budget()
      .flatMapLatest { graph ->
        graph?.localPreferences?.observe(BudgetName)?.map { it.orEmpty() } ?: flowOf(null)
      }
      .distinctUntilChanged()
}
