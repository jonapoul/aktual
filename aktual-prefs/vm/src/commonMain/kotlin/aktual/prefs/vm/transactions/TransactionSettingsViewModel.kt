package aktual.prefs.vm.transactions

import aktual.di.AppScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.prefs.TransactionPreferences
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.collectAsState
import aktual.prefs.vm.launchAndSet
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.StateFlow

@Stable
@ViewModelKey
@ContributesIntoMap(AppScope::class)
class TransactionSettingsViewModel
internal constructor(preferences: TransactionPreferences, viewModelContext: ViewModelContext) :
  ViewModel(viewModelContext.newScope()) {
  val state: StateFlow<TransactionSettingsState> =
    viewModelScope.launchMolecule(Immediate) {
      val alternateRowColours by collectAsState(preferences.alternateRowColours)
      val expandSplits by collectAsState(preferences.expandSplits)
      TransactionSettingsState(
        alternateRowColours =
          BooleanPreference(
            value = alternateRowColours,
            onChange = { launchAndSet(preferences.alternateRowColours, it) },
          ),
        expandSplits =
          BooleanPreference(
            value = expandSplits,
            onChange = { launchAndSet(preferences.expandSplits, it) },
          ),
      )
    }
}
