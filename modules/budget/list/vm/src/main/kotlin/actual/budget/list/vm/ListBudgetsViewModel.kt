package actual.budget.list.vm

import actual.core.colorscheme.ColorSchemePreferences
import actual.core.colorscheme.ColorSchemeType
import actual.core.versions.ActualVersions
import actual.core.versions.ActualVersionsStateHolder
import actual.log.Logger
import actual.login.model.LoginToken
import actual.url.model.ServerUrl
import actual.url.prefs.ServerUrlPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ListBudgetsViewModel.Factory::class)
class ListBudgetsViewModel @AssistedInject constructor(
  @Assisted tokenString: String,
  serverUrlPrefs: ServerUrlPreferences,
  versionsStateHolder: ActualVersionsStateHolder,
  colorSchemePreferences: ColorSchemePreferences,
  private val budgetListFetcher: BudgetListFetcher,
  private val logger: Logger,
) : ViewModel() {
  // Necessary because trying to pass a value class through dagger's assisted injection results in a KSP build failure.
  // See https://github.com/google/dagger/issues/4613
  // TODO: Rework to pass the token in directly when they fix it
  private val token = LoginToken(tokenString)

  val versions: StateFlow<ActualVersions> = versionsStateHolder.asStateFlow()

  val serverUrl: StateFlow<ServerUrl> = serverUrlPrefs
    .url
    .asFlow()
    .filterNotNull()
    .stateIn(viewModelScope, SharingStarted.Eagerly, ServerUrl.Demo)

  val themeType: StateFlow<ColorSchemeType> = colorSchemePreferences.stateFlow(viewModelScope)

  private val mutableState = MutableStateFlow<ListBudgetsState>(ListBudgetsState.Loading)
  val state: StateFlow<ListBudgetsState> = mutableState.asStateFlow()

  init {
    logger.d("init")
    fetchState()
  }

  fun retry() {
    logger.d("retry")
    fetchState()
  }

  private fun fetchState() {
    mutableState.update { ListBudgetsState.Loading }
    viewModelScope.launch {
      val result = budgetListFetcher.fetchBudgets(token)
      logger.d("Fetch budgets result = %s", result)
      val newState = when (result) {
        is FetchBudgetsResult.Failure -> ListBudgetsState.Failure(result.reason)
        is FetchBudgetsResult.Success -> ListBudgetsState.Success(result.budgets.toImmutableList())
      }
      mutableState.update { newState }
    }
  }

  @AssistedFactory
  interface Factory {
    fun create(tokenString: String): ListBudgetsViewModel
  }
}
