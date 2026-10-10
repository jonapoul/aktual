package aktual.about.vm

import aktual.about.data.ArtifactDetail
import aktual.about.data.LicensesLoadState
import aktual.about.data.LicensesRepository
import aktual.about.vm.SearchLicensesState.Results
import aktual.core.UrlOpener
import aktual.di.AppScope
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Stable
@ViewModelKey
@ContributesIntoMap(AppScope::class)
class SearchLicensesViewModel(
  private val licensesRepository: LicensesRepository,
  private val urlOpener: UrlOpener,
) : ViewModel() {
  private val mutableQuery = MutableStateFlow("")
  private val artifacts = MutableStateFlow<List<ArtifactDetail>>(emptyList())

  val query: StateFlow<String> = mutableQuery.asStateFlow()

  val state: StateFlow<SearchLicensesState> =
    combine(mutableQuery, artifacts, ::search)
      .stateIn(viewModelScope, Eagerly, initialValue = NoQuery)

  init {
    viewModelScope.launch {
      val loaded = (licensesRepository.loadLicenses() as? LicensesLoadState.Success)?.libraries
      artifacts.update { loaded.orEmpty() }
    }
  }

  fun openUrl(url: String) {
    urlOpener(url)
  }

  fun setQuery(query: String) {
    mutableQuery.update { query }
  }

  private fun search(query: String, artifacts: List<ArtifactDetail>): SearchLicensesState {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return NoQuery

    // Artifacts matching by name come before those only matching on other fields
    val matching =
      artifacts
        .filter { it.matches(trimmed) }
        .sortedBy { !it.displayName().contains(trimmed, ignoreCase = true) }

    return if (matching.isEmpty()) {
      NoResults
    } else {
      Results(trimmed, matching.toImmutableList())
    }
  }

  private fun ArtifactDetail.matches(query: String): Boolean =
    displayName().contains(query, ignoreCase = true) ||
      "$groupId:$artifactId".contains(query, ignoreCase = true) ||
      version.contains(query, ignoreCase = true) ||
      licenseName()?.contains(query, ignoreCase = true) == true
}
