package aktual.settings.vm.theme

import aktual.core.theme.CustomThemeCache
import aktual.core.theme.CustomThemeSummary
import aktual.core.theme.DarkTheme
import aktual.core.theme.MidnightTheme
import aktual.core.theme.Theme
import aktual.core.theme.ThemeApi
import aktual.core.theme.ThemePreferences
import aktual.core.theme.toId
import aktual.settings.vm.BooleanPreference
import aktual.settings.vm.ListPreference
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode.Immediate
import app.cash.molecule.launchMolecule
import dev.jonpoulton.preferences.core.asStateFlow
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

@Inject
@ViewModelKey(ThemeSettingsViewModel::class)
@ContributesIntoMap(AppScope::class)
class ThemeSettingsViewModel(
  private val preferences: ThemePreferences,
  private val themeApi: ThemeApi,
  private val cache: CustomThemeCache,
) : ViewModel() {
  private val useSystemDefault = preferences.useSystemDefault.asStateFlow(viewModelScope)
  private val nightTheme = preferences.nightTheme.asStateFlow(viewModelScope)
  private val constantTheme = preferences.constantTheme.asStateFlow(viewModelScope)

  private sealed interface LoadState {
    data object Loading : LoadState

    data class Failed(val reason: String) : LoadState

    data class Loaded(
      val summaries: List<CustomThemeSummary>,
      val states: Map<Theme.Id, CustomThemeState>,
    ) : LoadState
  }

  private val mutableEvents =
    MutableSharedFlow<ThemeSettingsEvent>(
      replay = 0,
      extraBufferCapacity = 1,
      onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
  val events: SharedFlow<ThemeSettingsEvent> = mutableEvents

  private val mutableLoadState = MutableStateFlow<LoadState>(LoadState.Loading)

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

  val catalogState: StateFlow<CatalogState> =
    viewModelScope.launchMolecule(Immediate) {
      val loadState by mutableLoadState.collectAsState()
      when (val s = loadState) {
        is LoadState.Loading -> {
          CatalogState.Loading
        }

        is LoadState.Failed -> {
          CatalogState.Failed(s.reason)
        }

        is LoadState.Loaded -> {
          val selected by constantTheme.collectAsState()
          CatalogState.Success(
            s.summaries
              .sortedBy { it.name }
              .map { summary -> toCustomThemeItem(summary, selected, s) }
              .toImmutableList()
          )
        }
      }
    }

  init {
    retry()
  }

  fun retry() {
    viewModelScope.launch { fetchCatalogThenThemes() }
  }

  fun clearCache() {
    viewModelScope.launch {
      cache.clear()
      fetchCatalogThenThemes()
      if (mutableLoadState.value is LoadState.Loaded) {
        mutableEvents.tryEmit(ThemeSettingsEvent.CacheRefreshed)
      }
    }
  }

  fun select(id: Theme.Id) {
    preferences.constantTheme.set(id)
  }

  fun setUseSystemDefault(value: Boolean) = preferences.useSystemDefault.set(value)

  fun setDarkTheme(value: Theme.Id) = preferences.nightTheme.set(value)

  @Composable
  private fun useSystemDefault(value: Boolean): BooleanPreference =
    remember(value) { BooleanPreference(value = value, enabled = true) }

  @Composable
  private fun darkTheme(value: Theme.Id, enabled: Boolean) =
    remember(value, enabled) {
      ListPreference(
        selected = value,
        values = persistentListOf(DarkTheme.id, MidnightTheme.id),
        enabled = enabled,
      )
    }

  private fun toCustomThemeItem(
    summary: CustomThemeSummary,
    selected: Theme.Id?,
    state: LoadState.Loaded,
  ): CatalogItem {
    val themeId = summary.repo.toId()
    return CatalogItem(
      id = themeId,
      summary = summary,
      isSelected = selected == themeId,
      state = state.states[themeId] ?: CustomThemeState.Fetching,
    )
  }

  private suspend fun fetchCatalogThenThemes() {
    mutableLoadState.update { LoadState.Loading }
    try {
      val summaries =
        cache.summaries().ifEmpty {
          val fetched = themeApi.fetchCatalog()
          cache.save(fetched)
          fetched
        }

      val initialFetchStates = summaries.associate { it.repo.toId() to CustomThemeState.Fetching }
      mutableLoadState.update { LoadState.Loaded(summaries, initialFetchStates) }

      summaries.map { summary -> viewModelScope.async { fetchTheme(summary) } }.awaitAll()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      logcat.e(e) { "Failed fetching theme catalog" }
      mutableLoadState.update { LoadState.Failed(e.requireMessage()) }
    }
  }

  private suspend fun fetchTheme(summary: CustomThemeSummary) {
    val themeId = summary.repo.toId()
    try {
      val cached = cache.theme(summary.repo)
      if (cached != null) {
        updateFetchState(themeId, CustomThemeState.Cached)
      } else {
        val theme = themeApi.fetchTheme(summary)
        cache.save(theme)
        updateFetchState(themeId, CustomThemeState.Cached)
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      logcat.e(e) { "Failed fetching theme ${summary.repo}" }
      updateFetchState(themeId, CustomThemeState.Failed(e.requireMessage()))
    }
  }

  private fun updateFetchState(themeId: Theme.Id, fetchState: CustomThemeState) {
    mutableLoadState.update { current ->
      if (current is LoadState.Loaded) {
        current.copy(states = current.states + (themeId to fetchState))
      } else {
        current
      }
    }
  }
}
