package actual.settings.vm

import actual.prefs.AppGlobalPreferences
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
  preferences: AppGlobalPreferences,
) : ViewModel() {
  private val colorSchemePref = preferences.regularColorScheme
  private val colorSchemeFlow = colorSchemePref.asStateFlow(viewModelScope)

  private val darkSchemePref = preferences.darkColorScheme
  private val darkSchemeFlow = darkSchemePref.asStateFlow(viewModelScope)

  private val showBottomBarPref = preferences.showBottomBar
  private val showBottomBar = showBottomBarPref.asStateFlow(viewModelScope)

  val prefValues: StateFlow<ImmutableList<PreferenceValue>> = viewModelScope.launchMolecule(Immediate) {
    val colorScheme by colorSchemeFlow.collectAsState()
    val darkScheme by darkSchemeFlow.collectAsState()
    val showBottomBar by showBottomBar.collectAsState()
    persistentListOf(
      PreferenceValue.Theme(ThemeConfig(colorScheme, darkScheme)),
      PreferenceValue.ShowBottomBar(showBottomBar),
    )
  }

  fun set(value: PreferenceValue) {
    when (value) {
      is PreferenceValue.Theme -> setColorScheme(value.config)
      is PreferenceValue.ShowBottomBar -> showBottomBarPref.set(value.show)
    }
  }

  private fun setColorScheme(config: ThemeConfig) {
    colorSchemePref.set(config.regular)
    darkSchemePref.set(config.dark)
  }
}
