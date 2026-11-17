package aktual.prefs.vm.dates

import aktual.budget.model.DbMetadataKey.Companion.BudgetName
import aktual.budget.utils.BudgetLocalPreferences
import aktual.core.utils.Calendar
import aktual.di.BudgetScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.prefs.BudgetFormatPreferences
import aktual.prefs.vm.ListPreference
import aktual.prefs.vm.collectAsState
import aktual.prefs.vm.launchAndSet
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.StateFlow

@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class DateSettingsViewModel
internal constructor(
  preferences: BudgetFormatPreferences,
  localPreferences: BudgetLocalPreferences,
  calendar: Calendar,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  val state: StateFlow<DateSettingsState> =
    viewModelScope.launchMolecule(Immediate) {
      val dateFormat by collectAsState(preferences.dateFormat)
      val firstDayOfWeek by collectAsState(preferences.firstDayOfWeek)
      val today by remember { calendar.observeToday() }.collectAsState(calendar.today())
      val budgetName by remember {
        localPreferences.observe(BudgetName)
      }
        .collectAsState(localPreferences[BudgetName])
      DateSettingsState(
        budgetName = budgetName.orEmpty(),
        dateFormat =
          ListPreference(
            value = dateFormat,
            onChange = { launchAndSet(preferences.dateFormat, it) },
          ),
        firstDayOfWeek =
          ListPreference(
            value = firstDayOfWeek,
            onChange = { launchAndSet(preferences.firstDayOfWeek, it) },
          ),
        today = today,
      )
    }
}
