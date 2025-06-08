package actual.budget.list.vm

import actual.account.model.LoginToken
import actual.budget.model.BudgetFiles
import actual.budget.model.BudgetId
import actual.budget.model.database
import actual.budget.model.metadata
import actual.core.model.ActualVersions
import actual.core.model.ActualVersionsStateHolder
import actual.core.model.ServerUrl
import actual.prefs.AppGlobalPreferences
import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.logging.Logger
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jonpoulton.preferences.core.asStateFlow
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel(assistedFactory = ListBudgetsViewModel.Factory::class)
class ListBudgetsViewModel @AssistedInject constructor(
  @Assisted tokenString: String,
  preferences: AppGlobalPreferences,
  versionsStateHolder: ActualVersionsStateHolder,
  private val budgetListFetcher: BudgetListFetcher,
  private val files: BudgetFiles,
  private val contexts: CoroutineContexts,
) : ViewModel() {
  // Necessary because trying to pass a value class through dagger's assisted injection results in a KSP build failure.
  // See https://github.com/google/dagger/issues/4613
  // TODO: Rework to pass the token in directly when they fix it
  private val token = LoginToken(tokenString)

  val versions: StateFlow<ActualVersions> = versionsStateHolder.asStateFlow()
  val serverUrl: StateFlow<ServerUrl?> = preferences.serverUrl.asStateFlow(viewModelScope)

  private val mutableState = MutableStateFlow<ListBudgetsState>(ListBudgetsState.Loading)
  val state: StateFlow<ListBudgetsState> = mutableState.asStateFlow()

  private val mutableDeletingState = MutableStateFlow<DeletingState>(DeletingState.Inactive)
  val deletingState: StateFlow<DeletingState> = mutableDeletingState.asStateFlow()

  private val mutableLocalFileExists = MutableStateFlow(emptyMap<BudgetId, Boolean>())
  val localFilesExist: StateFlow<Map<BudgetId, Boolean>> = mutableLocalFileExists.asStateFlow()

  private val mutableCloseDialog = MutableSharedFlow<Boolean>()
  val closeDialog: SharedFlow<Boolean> = mutableCloseDialog.asSharedFlow()

  init {
    Logger.d("init")
    fetchState()

    // Periodically check whether our files still exist
    viewModelScope.launch {
      state
        .filterIsInstance<ListBudgetsState.Success>()
        .map { state -> state.budgets.map { it.cloudFileId } }
        .collectLatest { budgetIds ->
          while (true) {
            delay(500.milliseconds)
            val existing = budgetIds.associateWith(::localFilesExist)
            mutableLocalFileExists.update { existing }
          }
        }
    }
  }

  fun retry() {
    Logger.d("retry")
    fetchState()
  }

  fun clearDeletingState() = mutableDeletingState.update { DeletingState.Inactive }

  fun deleteLocal(id: BudgetId) {
    Logger.d("deleteLocal $id")
    mutableDeletingState.update { DeletingState.Active(deletingLocal = true) }
    viewModelScope.launch {
      with(files) {
        val budgetDir = directory(id)
        if (fileSystem.exists(budgetDir)) {
          withContext(contexts.io) { fileSystem.deleteRecursively(budgetDir) }
        }

        while (fileSystem.exists(budgetDir)) {
          Logger.v("Still exists: $budgetDir")
          delay(200.milliseconds)
        }
        Logger.d("Successfully deleted $budgetDir")
        clearDeletingState()
        mutableCloseDialog.emit(true)
      }
    }
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

  private fun localFilesExist(id: BudgetId): Boolean = with(files) {
    listOf(
      database(id, mkdirs = false),
      metadata(id, mkdirs = false),
    ).all(fileSystem::exists)
  }

  @AssistedFactory
  interface Factory {
    fun create(tokenString: String): ListBudgetsViewModel
  }
}
