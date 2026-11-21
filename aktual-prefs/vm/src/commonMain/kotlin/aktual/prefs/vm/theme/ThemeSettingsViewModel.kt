package aktual.prefs.vm.theme

import aktual.api.client.ThemeApi
import aktual.core.model.ThemeId
import aktual.di.AppScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.prefs.Preference
import aktual.prefs.ThemePreferences
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.asLoadedStateFlow
import aktual.theme.Colors
import aktual.theme.CustomColors
import aktual.theme.CustomThemeCache
import aktual.theme.CustomThemeSummary
import aktual.theme.DarkColors
import aktual.theme.LightColors
import aktual.theme.MidnightColors
import aktual.theme.ThemeMode
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Stable
@ViewModelKey
@ContributesIntoMap(AppScope::class)
class ThemeSettingsViewModel
internal constructor(
  private val preferences: ThemePreferences,
  themeApi: ThemeApi,
  cache: CustomThemeCache,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  private val catalog = ThemeCatalog(themeApi, cache)
  private val useSystemDefault = preferences.useSystemDefault.asLoadedStateFlow(viewModelScope)
  private val lightTheme = preferences.dayTheme.asLoadedStateFlow(viewModelScope)
  private val darkTheme = preferences.nightTheme.asLoadedStateFlow(viewModelScope)
  private val constantTheme = preferences.constantTheme.asLoadedStateFlow(viewModelScope)
  private val catalogState = catalog.state.stateIn(viewModelScope, Eagerly, Initial)
  private val catalogOptions = MutableStateFlow(CatalogOptions())

  private val customThemes: StateFlow<ImmutableList<CustomColors>> =
    combine(cache.localThemes(), cache.downloadedThemes()) { local, downloaded ->
        (local + downloaded).sortedBy { it.name.lowercase() }.toImmutableList()
      }
      .stateIn(viewModelScope, Eagerly, persistentListOf())

  private val mutableEvents =
    MutableSharedFlow<ThemeSettingsEvent>(
      replay = 0,
      extraBufferCapacity = 1,
      onBufferOverflow = DROP_OLDEST,
    )

  val events: SharedFlow<ThemeSettingsEvent> = mutableEvents

  val state: StateFlow<ThemeSettingsState> =
    viewModelScope.launchMolecule(Immediate) {
      val useSystemDefault by useSystemDefault.collectAsState()
      val lightTheme by lightTheme.collectAsState()
      val darkTheme by darkTheme.collectAsState()
      val constantTheme by constantTheme.collectAsState()
      val customThemes by customThemes.collectAsState()
      val catalog by catalogState.collectAsState()
      val catalogOptions by catalogOptions.collectAsState()

      ThemeSettingsState(
        useSystemDefault = useSystemDefault(useSystemDefault),
        lightTheme =
          picker(lightTheme, LightThemes, customThemes, catalog, isLight = true, useSystemDefault),
        darkTheme =
          picker(darkTheme, DarkThemes, customThemes, catalog, isLight = false, useSystemDefault),
        constantTheme = constantTheme,
        localThemes = localThemes(customThemes),
        catalog = filtered(catalog, catalogOptions),
        catalogOptions = catalogOptions,
        previews = previews(useSystemDefault, lightTheme, darkTheme, constantTheme, customThemes),
        customThemeNames = customThemeNames(customThemes, catalog),
      )
    }

  init {
    viewModelScope.launch { catalog.load() }
  }

  fun select(id: ThemeId) {
    viewModelScope.launch { preferences.constantTheme.set(id) }
  }

  fun setUseSystemDefault(value: Boolean) {
    viewModelScope.launch { preferences.useSystemDefault.set(value) }
  }

  fun pickLightTheme(id: ThemeId) = pick(id, preferences.dayTheme)

  fun pickDarkTheme(id: ThemeId) = pick(id, preferences.nightTheme)

  fun setCatalogOptions(options: CatalogOptions) {
    catalogOptions.update { options }
  }

  fun refreshCatalog() {
    viewModelScope.launch {
      if (catalog.refresh()) mutableEvents.tryEmit(CatalogRefreshed)
    }
  }

  // Selects the theme once it's downloaded, unless matching the system
  fun selectCatalogTheme(summary: CustomThemeSummary) {
    viewModelScope.launch {
      download(summary) { theme, wasDownloaded ->
        if (!useSystemDefault.value) {
          preferences.constantTheme.set(theme.id)
        } else if (!wasDownloaded) {
          mutableEvents.tryEmit(ThemeSettingsEvent.ThemeDownloaded(summary.name))
        }
      }
    }
  }

  fun inspectCatalogTheme(summary: CustomThemeSummary) {
    viewModelScope.launch {
      download(summary) { theme, _ ->
        mutableEvents.tryEmit(ThemeSettingsEvent.InspectTheme(theme.id))
      }
    }
  }

  private fun pick(id: ThemeId, preference: Preference<ThemeId>) {
    viewModelScope.launch {
      val summary = catalogState.value.items.firstOrNull { it.id == id }?.summary
      if (summary == null) {
        preference.set(id)
        mutableEvents.tryEmit(ThemePicked)
      } else {
        download(summary) { theme, _ ->
          preference.set(theme.id)
          mutableEvents.tryEmit(ThemePicked)
        }
      }
    }
  }

  private suspend fun download(
    summary: CustomThemeSummary,
    onSuccess: suspend (theme: CustomColors, wasDownloaded: Boolean) -> Unit,
  ) {
    when (val result = catalog.download(summary)) {
      is Success -> onSuccess(result.theme, result.wasDownloaded)
      is Failure ->
        mutableEvents.tryEmit(ThemeSettingsEvent.DownloadFailed(summary.name, result.reason))
    }
  }

  @Composable
  private fun useSystemDefault(value: Boolean): BooleanPreference =
    remember(value) { BooleanPreference(value = value, enabled = true) }

  // Built-in themes first, then local ones, then the catalog
  @Composable
  private fun picker(
    value: ThemeId,
    builtIn: ImmutableList<Colors>,
    customThemes: ImmutableList<CustomColors>,
    catalog: CatalogState,
    isLight: Boolean,
    enabled: Boolean,
  ): SystemThemePicker =
    remember(value, builtIn, customThemes, catalog, isLight, enabled) {
      val builtInOptions = builtIn.map {
        ThemeOption(it.id, name = null, it.palette(), isLocal = false, download = null)
      }
      val localOptions =
        customThemes
          .filter { it.repo.isLocal && it.isLight == isLight }
          .map { ThemeOption(it.id, it.name, it.palette(), isLocal = true, download = null) }
      val mode = if (isLight) ThemeMode.Light else ThemeMode.Dark
      val catalogOptions =
        catalog.items
          .asSequence()
          .filter { it.summary.mode == mode }
          .sortedBy { it.summary.name.lowercase() }
          .map { item ->
            ThemeOption(
              id = item.id,
              name = item.summary.name,
              palette = item.summary.colors.toImmutableList(),
              isLocal = false,
              download = item.download,
            )
          }
      SystemThemePicker(
        value = value,
        options = (builtInOptions + localOptions + catalogOptions).toImmutableList(),
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
  private fun filtered(catalog: CatalogState, options: CatalogOptions): CatalogState =
    remember(catalog, options) {
      val items =
        catalog.items
          .asSequence()
          .filter { item ->
            when (options.mode) {
              All -> true
              Light -> item.summary.mode == Light
              Dark -> item.summary.mode == Dark
            }
          }
          .filter { item ->
            when (options.download) {
              AllThemes -> true
              DownloadedOnly -> item.download == Downloaded
              NotDownloaded -> item.download != Downloaded
            }
          }
          .sortedWith(
            when (options.sorting) {
              ByName -> compareBy<CatalogItem> { it.summary.name.lowercase() }
              ByRepo -> compareBy<CatalogItem> { it.summary.repo.id.lowercase() }
              DownloadedFirst ->
                compareBy<CatalogItem> { it.download != Downloaded }
                  .thenBy { it.summary.name.lowercase() }
            },
          )
          .toImmutableList()
      catalog.copy(items = items)
    }

  // A catalog theme that isn't downloaded yet has no colours to show, so it's left out
  @Composable
  private fun previews(
    useSystemDefault: Boolean,
    lightTheme: ThemeId,
    darkTheme: ThemeId,
    constantTheme: ThemeId?,
    customThemes: ImmutableList<CustomColors>,
  ): ImmutableList<ThemePreview> =
    remember(useSystemDefault, lightTheme, darkTheme, constantTheme, customThemes) {
      fun preview(id: ThemeId, mode: ThemePreviewMode): ThemePreview? {
        val colors: Colors =
          Colors.Defaults.firstOrNull { it.id == id }
            ?: customThemes.firstOrNull { it.id == id }
            ?: return null
        return ThemePreview(id, mode, colors.swatch())
      }

      if (useSystemDefault) {
          listOfNotNull(preview(lightTheme, Day), preview(darkTheme, Night))
        } else {
          listOfNotNull(preview(constantTheme ?: DarkColors.id, Always))
        }
        .toImmutableList()
    }

  @Composable
  private fun customThemeNames(customThemes: ImmutableList<CustomColors>, catalog: CatalogState) =
    remember(customThemes, catalog) {
      val catalogNames = catalog.items.map { it.id to it.summary.name }
      val customNames = customThemes.map { it.id to it.name }
      (catalogNames + customNames).toMap().toImmutableMap()
    }

  private companion object {
    val LightThemes = persistentListOf<Colors>(LightColors)
    val DarkThemes = persistentListOf<Colors>(DarkColors, MidnightColors)
  }
}
