package aktual.about.vm

import aktual.about.data.LicensesLoadState
import aktual.about.data.LicensesRepository
import aktual.core.UrlOpener
import aktual.di.AppScope
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

@Stable
@ViewModelKey
@ContributesIntoMap(AppScope::class)
class LicensesViewModel(
  private val licensesRepository: LicensesRepository,
  private val urlOpener: UrlOpener,
) : ViewModel() {
  private val mutableState = MutableStateFlow<LicensesState>(Loading)
  private val mutableSorting = MutableStateFlow<LicenseSorting>(ByArtifact)

  val sorting: StateFlow<LicenseSorting> = mutableSorting.asStateFlow()

  val licensesState: StateFlow<LicensesState> =
    viewModelScope.launchMolecule(Immediate) {
      val licensesState by mutableState.collectAsState()
      val sorting by mutableSorting.collectAsState()

      when (val licenses = licensesState) {
        is Loaded -> licenses.sortedBy(sorting)
        else -> licenses
      }
    }

  init {
    load()
  }

  fun load() {
    logcat.d { "load" }
    mutableState.update { Loading }
    viewModelScope.launch {
      val licensesState =
        when (val loadState = licensesRepository.loadLicenses()) {
          is Failure -> LicensesState.Error(loadState.cause)
          is Success -> loadState.toLicensesState()
        }

      mutableState.update { licensesState }
    }
  }

  private fun LicensesLoadState.Success.toLicensesState() =
    if (libraries.isEmpty()) {
      LicensesState.NoneFound
    } else {
      LicensesState.Loaded(libraries.toImmutableList())
    }

  fun openUrl(url: String) {
    logcat.d { "openUrl $url" }
    urlOpener(url)
  }

  fun setSorting(sorting: LicenseSorting) {
    logcat.d { "setSorting $sorting" }
    mutableSorting.update { sorting }
  }
}
