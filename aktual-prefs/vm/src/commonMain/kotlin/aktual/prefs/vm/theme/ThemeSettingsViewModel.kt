package aktual.prefs.vm.theme

import aktual.core.model.ThemeId
import aktual.core.theme.DarkTheme
import aktual.core.theme.MidnightTheme
import aktual.prefs.ThemePreferences
import aktual.prefs.asStateFlow
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.ListPreference
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode.Immediate
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Stable
@ViewModelKey
@ContributesIntoMap(AppScope::class)
class ThemeSettingsViewModel(private val preferences: ThemePreferences) : ViewModel() {
  private val useSystemDefault = preferences.useSystemDefault.asStateFlow(viewModelScope)
  private val nightTheme = preferences.nightTheme.asStateFlow(viewModelScope)
  private val constantTheme = preferences.constantTheme.asStateFlow(viewModelScope)

  val state: StateFlow<ThemeSettingsState> =
    viewModelScope.launchMolecule(Immediate) {
      val useSystemDefault by useSystemDefault.collectAsState()
      val nightTheme by nightTheme.collectAsState()
      val constantTheme by constantTheme.collectAsState()

      ThemeSettingsState(
        useSystemDefault = useSystemDefault(useSystemDefault),
        darkTheme = darkTheme(nightTheme, enabled = useSystemDefault),
        constantTheme = constantTheme,
      )
    }

  fun select(id: ThemeId) {
    viewModelScope.launch { preferences.constantTheme.set(id) }
  }

  fun setUseSystemDefault(value: Boolean) {
    viewModelScope.launch { preferences.useSystemDefault.set(value) }
  }

  fun setDarkTheme(value: ThemeId) {
    viewModelScope.launch { preferences.nightTheme.set(value) }
  }

  @Composable
  private fun useSystemDefault(value: Boolean): BooleanPreference =
    remember(value) { BooleanPreference(value = value, enabled = true) }

  @Composable
  private fun darkTheme(value: ThemeId, enabled: Boolean) =
    remember(value, enabled) {
      ListPreference(
        value = value,
        options = persistentListOf(DarkTheme.id, MidnightTheme.id),
        enabled = enabled,
      )
    }
}
