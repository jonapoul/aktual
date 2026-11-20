package aktual.prefs.vm.systemui

import aktual.budget.model.BudgetTab
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.ListPreference
import androidx.compose.runtime.Immutable

@Immutable
data class SystemUiSettingsState(
  val showStatusBar: BooleanPreference,
  val startTab: ListPreference<BudgetTab>,
)
