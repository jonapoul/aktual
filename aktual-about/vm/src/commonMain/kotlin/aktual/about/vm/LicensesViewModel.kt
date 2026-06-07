package aktual.about.vm

import aktual.about.data.LicensesLoadState
import aktual.about.data.LicensesRepository
import aktual.core.model.UrlOpener
import aktual.di.AppScope
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import app.cash.molecule.RecompositionMode.Immediate
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactoryKey
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

@Stable
@AssistedInject
class LicensesViewModel(
  @Assisted private val savedState: SavedStateHandle,
  private val licensesRepository: LicensesRepository,
  private val urlOpener: UrlOpener,
) : ViewModel() {
  @AssistedFactory
  @ViewModelAssistedFactoryKey(LicensesViewModel::class)
  @ContributesIntoMap(AppScope::class)
  fun interface Factory : ViewModelAssistedFactory {
    override fun create(extras: CreationExtras): LicensesViewModel =
      create(extras.createSavedStateHandle())

    fun create(@Assisted savedState: SavedStateHandle): LicensesViewModel
  }

  private val mutableState = MutableStateFlow<LicensesState>(LicensesState.Loading)

  private val searchTerm = savedState.getStateFlow(KEY_SEARCH_TERM, "")
  private val isSearchActive = savedState.getStateFlow(KEY_IS_SEARCH_ACTIVE, false)

  val licensesState: StateFlow<LicensesState> =
    viewModelScope.launchMolecule(Immediate) {
      val licensesState by mutableState.collectAsState()
      val searchTerm by searchTerm.collectAsState()
      val isSearchActive by isSearchActive.collectAsState()

      when (val licenses = licensesState) {
        is LicensesState.Loaded -> licenses.filteredBy(searchTerm, isSearchActive)
        else -> licenses
      }
    }

  init {
    load()
  }

  fun load() {
    logcat.d { "load" }
    mutableState.update { LicensesState.Loading }
    viewModelScope.launch {
      val licensesState =
        when (val loadState = licensesRepository.loadLicenses()) {
          is LicensesLoadState.Failure -> LicensesState.Error(loadState.cause)
          is LicensesLoadState.Success -> loadState.toLicensesState()
        }

      mutableState.update { licensesState }
    }
  }

  private fun LicensesLoadState.Success.toLicensesState() =
    if (libraries.isEmpty()) {
      LicensesState.NoneFound
    } else {
      LicensesState.Loaded(
        artifacts = libraries.toImmutableList(),
        filterText = "",
        isSearchActive = false,
      )
    }

  fun openUrl(url: String) {
    logcat.d { "openUrl $url" }
    urlOpener(url)
  }

  fun openSearch() {
    logcat.d { "openSearch" }
    savedState[KEY_IS_SEARCH_ACTIVE] = true
  }

  fun clearFilter() {
    logcat.d { "clearFilter" }
    savedState[KEY_SEARCH_TERM] = ""
    savedState[KEY_IS_SEARCH_ACTIVE] = false
  }

  fun setFilterText(text: String) {
    logcat.d { "setFilterText $text" }
    savedState[KEY_SEARCH_TERM] = text
  }

  private companion object {
    const val KEY_SEARCH_TERM = "search_term"
    const val KEY_IS_SEARCH_ACTIVE = "is_search_active"
  }
}
