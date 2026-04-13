package aktual.prefs.vm.theme.custom

import aktual.core.model.ThemeId
import aktual.core.theme.CustomTheme
import aktual.core.theme.CustomThemeCache
import aktual.core.theme.CustomThemeSummary
import aktual.core.theme.ThemeApi
import aktual.core.theme.ThemeMode
import aktual.core.theme.toId
import aktual.prefs.ThemePreferences
import aktual.prefs.asStateFlow
import alakazam.kotlin.requireMessage
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
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

@Stable
@ViewModelKey
@ContributesIntoMap(AppScope::class)
class CustomThemeSettingsViewModel(
  private val preferences: ThemePreferences,
  private val themeApi: ThemeApi,
  private val cache: CustomThemeCache,
) : ViewModel() {
  private val mutableIsFetchingCatalog = MutableStateFlow(false)
  private val mutableFilter = MutableStateFlow(ThemeFilter.All)
  private val mutableFailure = MutableStateFlow<CatalogState.Failed?>(null)
  private val mutableSummaries = MutableStateFlow(persistentListOf<CustomThemeSummary>())
  private val mutableCachedThemes = MutableStateFlow(persistentMapOf<ThemeId, CacheState>())
  private val selectedTheme = preferences.constantTheme.asStateFlow(viewModelScope)

  private val mutableEvents =
    MutableSharedFlow<CustomThemeEvent>(
      replay = 0,
      extraBufferCapacity = 1,
      onBufferOverflow = DROP_OLDEST,
    )

  val events: SharedFlow<CustomThemeEvent> = mutableEvents

  val filter: StateFlow<ThemeFilter> = mutableFilter.asStateFlow()

  val state: StateFlow<CatalogState> =
    viewModelScope.launchMolecule(Immediate) {
      val isFetchingCatalog by mutableIsFetchingCatalog.collectAsState()
      if (isFetchingCatalog) {
        return@launchMolecule CatalogState.Loading
      }

      val failure by mutableFailure.collectAsState()
      failure?.let {
        return@launchMolecule it
      }

      val filter by mutableFilter.collectAsState()
      val summaries by mutableSummaries.collectAsState()
      val themes by mutableCachedThemes.collectAsState()
      val selected by selectedTheme.collectAsState()

      val items =
        remember(filter, summaries, themes, selected) {
          buildItems(
            filter = filter,
            summaries = summaries,
            cachedThemes = themes,
            selected = selected,
          )
        }
      CatalogState.Success(items)
    }

  init {
    viewModelScope.launch { fetchCatalog(showSnackbar = false) }
  }

  fun clearCache() {
    viewModelScope.launch {
      cache.clear()
      mutableCachedThemes.update { persistentMapOf() }
      fetchCatalog(showSnackbar = true)
    }
  }

  fun select(summary: CustomThemeSummary) {
    viewModelScope.launch {
      fetchThemeImpl(
        summary = summary,
        onSuccess = { theme -> viewModelScope.launch { preferences.constantTheme.set(theme.id) } },
        onFailure = { reason -> logcat.e { "Failed selecting theme: $reason" } },
      )
    }
  }

  fun setFilter(filter: ThemeFilter) {
    mutableFilter.update { filter }
  }

  fun fetchAndNavigate(summary: CustomThemeSummary) {
    viewModelScope.launch {
      fetchThemeImpl(
        summary = summary,
        onSuccess = { theme -> mutableEvents.tryEmit(CustomThemeEvent.InspectTheme(theme.id)) },
        onFailure = { reason ->
          mutableEvents.tryEmit(CustomThemeEvent.FailedFetching(reason, summary.name))
        },
      )
    }
  }

  private suspend fun fetchThemeImpl(
    summary: CustomThemeSummary,
    onSuccess: (CustomTheme) -> Unit,
    onFailure: (String) -> Unit,
  ) {
    val themeId = summary.repo.toId()
    try {
      val cached = cache.theme(summary.repo)
      if (cached != null) {
        updateFetchState(themeId, CacheState.Cached(summary))
        onSuccess(cached)
      } else {
        mutableCachedThemes.update { states -> states.put(themeId, CacheState.Fetching) }
        val theme = themeApi.fetchTheme(summary)
        cache.save(theme)
        updateFetchState(themeId, CacheState.Cached(summary))
        onSuccess(theme)
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      logcat.e(e) { "Failed fetching theme ${summary.repo}" }
      val reason = e.requireMessage()
      updateFetchState(themeId, CacheState.Failed(reason))
      onFailure(reason)
    }
  }

  private fun buildItems(
    filter: ThemeFilter,
    summaries: List<CustomThemeSummary>,
    cachedThemes: Map<ThemeId, CacheState>,
    selected: ThemeId?,
  ): ImmutableList<CatalogItem> =
    summaries
      .asSequence()
      .filter { summary -> byThemeMode(filter, summary) }
      .sortedBy { summary -> summary.name }
      .map { summary -> toCatalogItem(summary, selected, cachedThemes) }
      .toImmutableList()

  private fun toCatalogItem(
    summary: CustomThemeSummary,
    selected: ThemeId?,
    cachedThemes: Map<ThemeId, CacheState>,
  ): CatalogItem {
    val themeId = summary.repo.toId()
    return CatalogItem(
      id = themeId,
      summary = summary,
      isSelected = selected == themeId,
      state = cachedThemes[themeId] ?: CacheState.Remote,
    )
  }

  private suspend fun fetchCatalog(showSnackbar: Boolean) {
    mutableIsFetchingCatalog.update { true }
    mutableSummaries.update { persistentListOf() }
    mutableFailure.update { null }
    try {
      val summaries =
        cache
          .summaries()
          .ifEmpty { themeApi.fetchCatalog().also { cache.save(it) } }
          .toPersistentList()
      mutableSummaries.update { summaries }

      val cacheStates =
        summaries.associate { s -> s.repo.toId() to cacheState(s) }.toPersistentMap()
      mutableCachedThemes.update { cacheStates }

      if (showSnackbar) {
        mutableEvents.tryEmit(CustomThemeEvent.CacheRefreshed)
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      logcat.e(e) { "Failed fetching theme catalog" }
      mutableFailure.update { FetchingCatalog }
    } finally {
      mutableIsFetchingCatalog.update { false }
    }
  }

  private suspend fun cacheState(summary: CustomThemeSummary): CacheState {
    mutableCachedThemes.value[summary.repo.toId()]?.let {
      return it
    }
    return if (cache.theme(summary.repo) != null) CacheState.Cached(summary) else CacheState.Remote
  }

  private fun updateFetchState(themeId: ThemeId, fetchState: CacheState) {
    mutableCachedThemes.update { themes -> themes.put(themeId, fetchState) }
  }

  private fun byThemeMode(filter: ThemeFilter, summary: CustomThemeSummary): Boolean =
    when (filter) {
      ThemeFilter.All -> true
      ThemeFilter.Light -> summary.mode == ThemeMode.Light
      ThemeFilter.Dark -> summary.mode == ThemeMode.Dark
    }
}
