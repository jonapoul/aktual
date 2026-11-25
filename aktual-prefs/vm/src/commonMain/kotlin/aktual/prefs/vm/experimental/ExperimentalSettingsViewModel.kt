package aktual.prefs.vm.experimental

import aktual.budget.model.SyncedPrefKey.FeatureFlag
import aktual.di.BudgetScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.prefs.BudgetFeatureFlags
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.collectAsState
import aktual.prefs.vm.launchAndSet
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.StateFlow

@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class ExperimentalSettingsViewModel
internal constructor(
  flags: BudgetFeatureFlags,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  val state: StateFlow<ExperimentalSettingsState> =
    viewModelScope.launchMolecule(Immediate) {
      val values = FeatureFlag.entries.associateWith { flag -> collectAsState(flags[flag]).value }
      ExperimentalSettingsState(
        features =
          FeatureFlag.entries
            .map { flag ->
              ExperimentalFeature(
                flag = flag,
                isSupported = flag in Supported,
                preference =
                  BooleanPreference(
                    value = values.getValue(flag),
                    enabled = flag in Supported,
                    visible = isVisible(flag, values),
                    onChange = { value -> launchAndSet(flags[flag], value) },
                  ),
              )
            }
            .toImmutableList(),
      )
    }

  private fun isVisible(flag: FeatureFlag, values: Map<FeatureFlag, Boolean>): Boolean =
    when (flag) {
      GoalTemplatesUi -> values.getValue(GoalTemplates)
      GoalTemplates,
      ActionTemplating,
      FormulaMode,
      CurrencySupport,
      MobileCalculator,
      NewSidebarUi,
      SankeyReport,
      BalanceForecastReport,
      BudgetAnalysisReport,
      MonteCarloReport -> true
    }

  private companion object {
    // Features Aktual has, so toggling them here does what it says. The rest only sync to upstream
    val Supported =
      setOf<FeatureFlag>(
        FormulaMode,
        CurrencySupport,
        SankeyReport,
        BalanceForecastReport,
        BudgetAnalysisReport,
        MonteCarloReport,
      )
  }
}
