package dev.jonpoulton.actual.listbudgets.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jonpoulton.actual.core.model.ActualVersions
import dev.jonpoulton.actual.core.model.ServerUrl
import dev.jonpoulton.actual.core.state.ActualVersionsStateHolder
import dev.jonpoulton.actual.login.prefs.LoginPreferences
import dev.jonpoulton.actual.serverurl.prefs.ServerUrlPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ListBudgetsViewModel @Inject internal constructor(
  serverUrlPrefs: ServerUrlPreferences,
  loginPrefs: LoginPreferences,
  versionsStateHolder: ActualVersionsStateHolder,
  private val budgetsFetcher: BudgetsFetcher,
) : ViewModel() {
  val versions: StateFlow<ActualVersions> = versionsStateHolder.state

  val serverUrl: StateFlow<ServerUrl> = serverUrlPrefs
    .url
    .asFlow()
    .filterNotNull()
    .stateIn(viewModelScope, SharingStarted.Eagerly, ServerUrl.Demo)

  val state: StateFlow<ListBudgetsState> = flowOf(ListBudgetsState.Loading)
    .stateIn(viewModelScope, SharingStarted.Eagerly, ListBudgetsState.Loading)
}
