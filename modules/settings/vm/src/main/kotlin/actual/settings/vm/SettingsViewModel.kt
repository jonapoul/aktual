package actual.settings.vm

import actual.core.colorscheme.ColorSchemePreferences
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode.Immediate
import app.cash.molecule.launchMolecule
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jonpoulton.preferences.core.asStateFlow
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject internal constructor(
  colorSchemePrefs: ColorSchemePreferences,
) : ViewModel() {
  private val colorSchemePref = colorSchemePrefs.type
  private val colorSchemeFlow = colorSchemePref.asStateFlow(viewModelScope)

  val prefValues: StateFlow<ImmutableList<PreferenceValue>> = viewModelScope.launchMolecule(Immediate) {
    val colorScheme by colorSchemeFlow.collectAsState()
    persistentListOf(
      PreferenceValue.Theme(colorScheme),
    )
  }

  fun set(value: PreferenceValue) {
    when (value) {
      is PreferenceValue.Theme -> colorSchemePref.set(value.type)
    }
  }
}
