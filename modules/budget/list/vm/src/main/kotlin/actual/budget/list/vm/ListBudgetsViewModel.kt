package actual.budget.list.vm

import actual.account.model.LoginToken
import actual.core.model.ActualVersions
import actual.core.model.ActualVersionsStateHolder
import actual.core.model.ServerUrl
import actual.prefs.AppLocalPreferences
import alakazam.kotlin.logging.Logger
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jonpoulton.preferences.core.asStateFlow
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ListBudgetsViewModel.Factory::class)
class ListBudgetsViewModel @AssistedInject constructor(
  @Assisted tokenString: String,
  preferences: AppLocalPreferences,
  versionsStateHolder: ActualVersionsStateHolder,
  private val budgetListFetcher: BudgetListFetcher,
) : ViewModel() {
  // Necessary because trying to pass a value class through dagger's assisted injection results in a KSP build failure.
  // See https://github.com/google/dagger/issues/4613
  // TODO: Rework to pass the token in directly when they fix it
  private val token = LoginToken(tokenString)

  val versions: StateFlow<ActualVersions> = versionsStateHolder.asStateFlow()
  val serverUrl: StateFlow<ServerUrl?> = preferences.serverUrl.asStateFlow(viewModelScope)

  private val mutableState = MutableStateFlow<ListBudgetsState>(ListBudgetsState.Loading)
  val state: StateFlow<ListBudgetsState> = mutableState.asStateFlow()

  init {
    Logger.d("init")
    fetchState()
  }

  fun retry() {
    Logger.d("retry")
    fetchState()
  }

  private fun fetchState() {
    mutableState.update { ListBudgetsState.Loading }
    viewModelScope.launch {
      val result = budgetListFetcher.fetchBudgets(token)
      Logger.d("Fetch budgets result = %s", result)
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
