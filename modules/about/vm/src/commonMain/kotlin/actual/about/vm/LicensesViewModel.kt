/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package actual.about.vm

import actual.about.data.LicensesLoadState
import actual.about.data.LicensesRepository
import actual.core.di.ViewModelKey
import actual.core.di.ViewModelScope
import actual.core.model.UrlOpener
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode.Immediate
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

@Inject
@ViewModelKey(LicensesViewModel::class)
@ContributesIntoMap(ViewModelScope::class)
class LicensesViewModel internal constructor(
  private val licensesRepository: LicensesRepository,
  private val urlOpener: UrlOpener,
) : ViewModel() {
  private val mutableState = MutableStateFlow<LicensesState>(LicensesState.Loading)
  private val showSearchBar = MutableStateFlow(value = false)
  private val searchTerm = MutableStateFlow(value = "")

  val searchBarState: StateFlow<SearchBarState> = viewModelScope.launchMolecule(Immediate) {
    val showSearchBar by showSearchBar.collectAsState()
    val searchTerm by searchTerm.collectAsState()
    if (showSearchBar) SearchBarState.Visible(searchTerm) else SearchBarState.Gone
  }

  val licensesState: StateFlow<LicensesState> = viewModelScope.launchMolecule(Immediate) {
    val licensesState by mutableState.collectAsState()
    val searchBarState by searchBarState.collectAsState()
    when (val searchState = searchBarState) {
      SearchBarState.Gone -> licensesState
      is SearchBarState.Visible -> when (val licenses = licensesState) {
        is LicensesState.Loaded -> licenses.filteredBy(searchState.text)
        else -> licenses
      }
    }
  }

  init {
    load()
  }

  fun load() {
    logcat.d { "load" }
    mutableState.update { LicensesState.Loading }
    viewModelScope.launch {
      val licensesState = when (val loadState = licensesRepository.loadLicenses()) {
        is LicensesLoadState.Failure -> LicensesState.Error(loadState.cause)
        is LicensesLoadState.Success -> loadState.toLicensesState()
      }

      mutableState.update { licensesState }
    }
  }

  private fun LicensesLoadState.Success.toLicensesState() = if (libraries.isEmpty()) {
    LicensesState.NoneFound
  } else {
    LicensesState.Loaded(libraries.toImmutableList())
  }

  fun openUrl(url: String) {
    logcat.d { "openUrl $url" }
    urlOpener(url)
  }

  fun toggleSearchBar() {
    logcat.d { "toggleSearchBar existing=${showSearchBar.value}" }
    showSearchBar.update { alreadyVisible -> !alreadyVisible }
  }

  fun setSearchText(text: String) {
    logcat.d { "setSearchText $text" }
    searchTerm.update { text }
  }
}
