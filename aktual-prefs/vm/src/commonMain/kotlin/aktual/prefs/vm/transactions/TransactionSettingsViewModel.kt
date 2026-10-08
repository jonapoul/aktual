package aktual.prefs.vm.transactions

import aktual.di.AppScope
import aktual.prefs.TransactionPreferences
import aktual.prefs.asStateFlow
import aktual.prefs.vm.BooleanPreference
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Stable
@ViewModelKey
@ContributesIntoMap(AppScope::class)
class TransactionSettingsViewModel(preferences: TransactionPreferences) : ViewModel() {
  private val alternateRowColours = preferences.alternateRowColours.asStateFlow(viewModelScope)

  val state: StateFlow<TransactionSettingsState> =
    viewModelScope.launchMolecule(Immediate) {
      val alternateRowColours by alternateRowColours.collectAsState()
      TransactionSettingsState(
        alternateRowColours =
          BooleanPreference(
            value = alternateRowColours,
            onChange = { viewModelScope.launch { preferences.alternateRowColours.set(it) } },
          ),
      )
    }
}
