package aktual.prefs.vm.theme

import aktual.core.model.ThemeId
import aktual.core.theme.CustomThemeCache
import aktual.core.theme.DarkColors
import aktual.core.theme.MidnightColors
import aktual.di.AppScope
import aktual.prefs.ThemePreferences
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.ListPreference
import aktual.prefs.vm.asLoadedStateFlow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Stable
@ViewModelKey
@ContributesIntoMap(AppScope::class)
class ThemeSettingsViewModel(
  private val preferences: ThemePreferences,
  cache: CustomThemeCache,
) : ViewModel() {
  private val useSystemDefault = preferences.useSystemDefault.asLoadedStateFlow(viewModelScope)
  private val nightTheme = preferences.nightTheme.asLoadedStateFlow(viewModelScope)
  private val constantTheme = preferences.constantTheme.asLoadedStateFlow(viewModelScope)
  private val localThemes =
    cache
      .localThemes()
      .map { themes ->
        themes.map { LocalTheme(it.id, it.name) }.sortedBy { it.name.lowercase() }.toImmutableList()
      }
      .stateIn(viewModelScope, Eagerly, persistentListOf())

  val state: StateFlow<ThemeSettingsState> =
    viewModelScope.launchMolecule(Immediate) {
      val useSystemDefault by useSystemDefault.collectAsState()
      val nightTheme by nightTheme.collectAsState()
      val constantTheme by constantTheme.collectAsState()
      val localThemes by localThemes.collectAsState()

      ThemeSettingsState(
        useSystemDefault = useSystemDefault(useSystemDefault),
        darkTheme = darkTheme(nightTheme, enabled = useSystemDefault),
        constantTheme = constantTheme,
        localThemes = localThemes,
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
        options = persistentListOf(DarkColors.id, MidnightColors.id),
        enabled = enabled,
      )
    }
}
