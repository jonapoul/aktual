package aktual.prefs.vm.search

import aktual.core.l10n.StringLookup
import aktual.core.utils.LocationProvider
import aktual.di.AppScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.prefs.vm.search.SearchSettingsState.Results
import aktual.prefs.vm.security.ShouldShowSecuritySettings
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactoryKey
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Stable
@AssistedInject
class SearchSettingsViewModel
internal constructor(
  @Assisted private val savedState: SavedStateHandle,
  private val strings: StringLookup,
  private val locationProvider: LocationProvider,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  @AssistedFactory
  @ViewModelAssistedFactoryKey(SearchSettingsViewModel::class)
  @ContributesIntoMap(AppScope::class)
  fun interface Factory : ViewModelAssistedFactory {
    override fun create(extras: CreationExtras): SearchSettingsViewModel =
      create(extras.createSavedStateHandle())

    fun create(@Assisted savedState: SavedStateHandle): SearchSettingsViewModel
  }

  val query: StateFlow<String> = savedState.getStateFlow(KEY_QUERY, initialValue = "")

  private val settings = MutableStateFlow<List<SettingsSearchResult>>(emptyList())

  val state: StateFlow<SearchSettingsState> =
    combine(query, settings, ::search).stateIn(viewModelScope, Eagerly, initialValue = NoQuery)

  init {
    viewModelScope.launch {
      val resolved = SettingsIndex.flatMap { (page, entry) -> resolve(page, entry) }
      settings.update { resolved }
    }
  }

  fun setQuery(query: String) {
    savedState[KEY_QUERY] = query
  }

  private suspend fun resolve(page: SettingsPage, entry: SettingsIndexEntry) =
    if (page == Security && !ShouldShowSecuritySettings) {
      emptyList()
    } else {
      buildList { addEntry(page, entry, path = emptyList(), isPage = true) }
    }

  private suspend fun MutableList<SettingsSearchResult>.addEntry(
    page: SettingsPage,
    entry: SettingsIndexEntry,
    path: List<String>,
    isPage: Boolean,
  ) {
    if (entry.needsLocation && !locationProvider.isSupported) return
    val title = strings[entry.title]
    val kind: SettingsSearchKind =
      when {
        isPage -> Page
        entry.children.isNotEmpty() -> Group
        else -> Preference
      }
    add(SettingsSearchResult(title, kind, page, path.toImmutableList()))
    entry.children.forEach { child -> addEntry(page, child, path + title, isPage = false) }
  }

  private fun search(query: String, settings: List<SettingsSearchResult>): SearchSettingsState {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return NoQuery

    // Pages before groups before single preferences, otherwise in screen order
    val matching =
      settings
        .filter { it.title.contains(trimmed, ignoreCase = true) }
        .sortedBy { it.kind }
        .toImmutableList()

    return if (matching.isEmpty()) {
      NoResults
    } else {
      Results(trimmed, matching)
    }
  }

  private companion object {
    const val KEY_QUERY = "query"
  }
}
