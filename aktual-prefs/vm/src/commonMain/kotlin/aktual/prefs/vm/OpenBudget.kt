package aktual.prefs.vm

import aktual.budget.model.DbMetadataKey.Companion.BudgetName
import aktual.budget.model.SyncedPrefKey.FeatureFlag
import aktual.di.AppScope
import aktual.di.BudgetScope
import aktual.di.RunLevelState
import aktual.prefs.BudgetFeatureFlags
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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

// The open budget's experimental flags that are on, empty when none is open
fun interface OpenBudgetFlags {
  fun observe(): Flow<Set<FeatureFlag>>
}

/** Gives code outside the budget graph, like [OpenBudgetFlagsImpl], the open budget's flags */
@ContributesTo(BudgetScope::class)
interface BudgetFeatureFlagsGraph {
  val budgetFeatureFlags: BudgetFeatureFlags
}

@Inject
@ContributesBinding(AppScope::class)
class OpenBudgetFlagsImpl internal constructor(private val runLevelState: RunLevelState) :
  OpenBudgetFlags {
  override fun observe(): Flow<Set<FeatureFlag>> =
    runLevelState
      .budget()
      .flatMapLatest { graph ->
        val flags = (graph as? BudgetFeatureFlagsGraph)?.budgetFeatureFlags
        if (flags == null) {
          flowOf(emptySet())
        } else {
          val values = FeatureFlag.entries.map { flag -> flags[flag].asFlow().map { flag to it } }
          combine(values) { pairs -> pairs.filter { it.second }.map { it.first }.toSet() }
        }
      }
      .distinctUntilChanged()
}
