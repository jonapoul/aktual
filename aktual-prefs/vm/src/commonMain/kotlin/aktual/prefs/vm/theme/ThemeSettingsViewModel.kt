package aktual.prefs.vm.theme

import aktual.core.model.ThemeId
import aktual.core.theme.CustomColors
import aktual.core.theme.CustomThemeCache
import aktual.core.theme.DarkColors
import aktual.core.theme.LightColors
import aktual.core.theme.MidnightColors
import aktual.di.AppScope
import aktual.di.ViewModelContext
import aktual.di.newScope
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
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Stable
@ViewModelKey
@ContributesIntoMap(AppScope::class)
class ThemeSettingsViewModel
internal constructor(
  private val preferences: ThemePreferences,
  private val cache: CustomThemeCache,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  private val useSystemDefault = preferences.useSystemDefault.asLoadedStateFlow(viewModelScope)
  private val dayTheme = preferences.dayTheme.asLoadedStateFlow(viewModelScope)
  private val nightTheme = preferences.nightTheme.asLoadedStateFlow(viewModelScope)
  private val constantTheme = preferences.constantTheme.asLoadedStateFlow(viewModelScope)

  // Local and downloaded themes. Picking one from the catalog sets the constant theme, so reload
  // then to pick up the download
  private val customThemes: StateFlow<ImmutableList<CustomColors>> =
    combine(cache.localThemes(), preferences.constantTheme.asFlow()) { local, _ -> local }
      .map { local -> loadCustomThemes(local) }
      .stateIn(viewModelScope, Eagerly, persistentListOf())

  val state: StateFlow<ThemeSettingsState> =
    viewModelScope.launchMolecule(Immediate) {
      val useSystemDefault by useSystemDefault.collectAsState()
      val dayTheme by dayTheme.collectAsState()
      val nightTheme by nightTheme.collectAsState()
      val constantTheme by constantTheme.collectAsState()
      val customThemes by customThemes.collectAsState()

      ThemeSettingsState(
        useSystemDefault = useSystemDefault(useSystemDefault),
        dayTheme =
          systemTheme(dayTheme, LightThemes, customThemes, isLight = true, useSystemDefault),
        darkTheme =
          systemTheme(nightTheme, DarkThemes, customThemes, isLight = false, useSystemDefault),
        constantTheme = constantTheme,
        localThemes = localThemes(customThemes),
        customThemeNames = customThemeNames(customThemes),
      )
    }

  fun select(id: ThemeId) {
    viewModelScope.launch { preferences.constantTheme.set(id) }
  }

  fun setUseSystemDefault(value: Boolean) {
    viewModelScope.launch { preferences.useSystemDefault.set(value) }
  }

  fun setLightTheme(value: ThemeId) {
    viewModelScope.launch { preferences.dayTheme.set(value) }
  }

  fun setDarkTheme(value: ThemeId) {
    viewModelScope.launch { preferences.nightTheme.set(value) }
  }

  private suspend fun loadCustomThemes(local: List<CustomColors>): ImmutableList<CustomColors> {
    val downloaded = cache.repos().filterNot { it.isLocal }.mapNotNull { cache.theme(it) }
    return (local + downloaded)
      .distinctBy { it.id }
      .sortedBy { it.name.lowercase() }
      .toImmutableList()
  }

  @Composable
  private fun useSystemDefault(value: Boolean): BooleanPreference =
    remember(value) { BooleanPreference(value = value, enabled = true) }

  @Composable
  private fun systemTheme(
    value: ThemeId,
    builtIn: ImmutableList<ThemeId>,
    customThemes: ImmutableList<CustomColors>,
    isLight: Boolean,
    enabled: Boolean,
  ): ListPreference<ThemeId> =
    remember(value, builtIn, customThemes, isLight, enabled) {
      val custom = customThemes.filter { it.isLight == isLight }.map { it.id }
      ListPreference(
        value = value,
        options = (builtIn + custom).toPersistentList(),
        enabled = enabled,
      )
    }

  @Composable
  private fun localThemes(customThemes: ImmutableList<CustomColors>): ImmutableList<LocalTheme> =
    remember(customThemes) {
      customThemes
        .filter { it.repo.isLocal }
        .map { LocalTheme(it.id, it.name, it.swatch()) }
        .toImmutableList()
    }

  @Composable
  private fun customThemeNames(customThemes: ImmutableList<CustomColors>) =
    remember(customThemes) { customThemes.associate { it.id to it.name }.toImmutableMap() }

  private companion object {
    val LightThemes = persistentListOf(LightColors.id)
    val DarkThemes = persistentListOf(DarkColors.id, MidnightColors.id)
  }
}
