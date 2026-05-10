package aktual.budget.list.vm

import aktual.api.client.SyncApi
import aktual.api.model.sync.DeleteUserFileRequest
import aktual.api.model.sync.UserFile
import aktual.budget.list.vm.ListBudgetsState.Failure
import aktual.budget.list.vm.ListBudgetsState.Loading
import aktual.budget.list.vm.ListBudgetsState.Success
import aktual.budget.model.BudgetFiles
import aktual.budget.model.BudgetId
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import aktual.core.model.UrlOpener
import aktual.di.LoggedInScope
import aktual.di.RunLevelController
import aktual.prefs.AppPreferences
import aktual.prefs.asStateFlow
import aktual.prefs.delete
import alakazam.kotlin.CoroutineContexts
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logcat.logcat

@Stable
@ViewModelKey
@ContributesIntoMap(LoggedInScope::class)
class ListBudgetsViewModel(
  private val token: Token,
  private val preferences: AppPreferences,
  private val budgetListFetcher: BudgetListFetcher,
  private val reconciler: BudgetReconciler,
  private val files: BudgetFiles,
  private val contexts: CoroutineContexts,
  private val syncApi: SyncApi,
  private val urlOpener: UrlOpener,
  private val appPreferences: AppPreferences,
  private val runLevels: RunLevelController,
) : ViewModel() {
  private val mutableEvent = MutableSharedFlow<ListBudgetsEvent>(extraBufferCapacity = 1)
  val event: Flow<ListBudgetsEvent> = mutableEvent.asSharedFlow()

  val serverUrl: StateFlow<ServerUrl?> = preferences.serverUrl.asStateFlow(viewModelScope)

  private val mutableState =
    MutableStateFlow<ListBudgetsState>(Loading(preferences.mostRecentNumBudgets.default))

  val state: StateFlow<ListBudgetsState> = mutableState.asStateFlow()

  private val mutableDeletingState = MutableStateFlow<DeletingState>(DeletingState.Inactive)
  val deletingState: StateFlow<DeletingState> = mutableDeletingState.asStateFlow()

  private val mutableCloseDialog = MutableSharedFlow<Boolean>()
  val closeDialog: SharedFlow<Boolean> = mutableCloseDialog.asSharedFlow()

  private var lastUserFiles: List<UserFile>? = null

  init {
    logcat.d { "init" }
    fetchState()

    // Periodically re-reconcile so newly downloaded / deleted local files update the list
    viewModelScope.launch {
      while (true) {
        delay(500.milliseconds)
        applyReconcile(lastUserFiles)
      }
    }
  }

  fun retry() {
    logcat.d { "retry" }
    fetchState()
  }

  fun onSyncComplete(id: BudgetId) {
    viewModelScope.launch {
      appPreferences.lastOpenedBudgetId.set(id)
      mutableEvent.emit(ListBudgetsEvent.NavToBudget)
    }
  }

  fun clearDeletingState() = mutableDeletingState.update { DeletingState.Inactive }

  fun deleteRemote(id: BudgetId) {
    logcat.d { "deleteRemote $id" }
    mutableDeletingState.update { DeletingState.Active(deletingRemote = true) }

    viewModelScope.launch {
      // delete local
      with(files) {
        val budgetDir = directory(id)
        withContext(contexts.io) { fileSystem.deleteRecursively(budgetDir) }
      }
      clearLastOpenedIfMatches(id)

      // delete remote
      try {
        val request = DeleteUserFileRequest(id, token)
        syncApi.delete(request)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed deleting $id" }
      }

      // close dialog
      logcat.d { "Successfully deleted $id" }
      clearDeletingState()
      mutableCloseDialog.emit(true)

      // re-fetch from server
      fetchState()
    }
  }

  fun deleteLocal(id: BudgetId) {
    logcat.d { "deleteLocal $id" }
    mutableDeletingState.update { DeletingState.Active(deletingLocal = true) }
    viewModelScope.launch {
      with(files) {
        val budgetDir = directory(id)
        if (fileSystem.exists(budgetDir)) {
          withContext(contexts.io) { fileSystem.deleteRecursively(budgetDir) }
        }

        logcat.d { "Successfully deleted $budgetDir" }
        clearLastOpenedIfMatches(id)
        clearDeletingState()
        mutableCloseDialog.emit(true)
        applyReconcile(lastUserFiles)
      }
    }
  }

  fun open(serverUrl: ServerUrl?) {
    val url = serverUrl?.toString() ?: return
    urlOpener(url)
  }

  fun logOut() {
    viewModelScope.launch {
      preferences.token.delete()
      runLevels.onLoggedOut()
      mutableEvent.emit(ListBudgetsEvent.LogOut)
    }
  }

  private fun fetchState() {
    viewModelScope.launch {
      val mostRecentNumBudgets = preferences.mostRecentNumBudgets.get()
      mutableState.update { Loading(mostRecentNumBudgets) }

      val result = budgetListFetcher.fetchBudgets(token)
      logcat.d { "Fetch budgets result = $result" }
      val newState =
        when (result) {
          is FetchBudgetsResult.Failure -> {
            // Even on failure, surface anything we already have locally as Unknown/Local
            val budgets = reconciler.reconcile(remote = null).toImmutableList()
            lastUserFiles = null
            if (budgets.isEmpty()) Failure(result.reason) else Success(budgets)
          }

          is FetchBudgetsResult.Success -> {
            lastUserFiles = result.userFiles
            val budgets = reconciler.reconcile(result.userFiles).toImmutableList()
            preferences.mostRecentNumBudgets.set(budgets.size)
            Success(budgets)
          }
        }
      mutableState.update { newState }
    }
  }

  private suspend fun applyReconcile(userFiles: List<UserFile>?) {
    if (mutableState.value !is Success) return
    val budgets = reconciler.reconcile(userFiles).toImmutableList()
    val current = mutableState.value
    if (current is Success && current.budgets == budgets) return
    mutableState.update { Success(budgets) }
  }

  private suspend fun clearLastOpenedIfMatches(id: BudgetId) {
    if (preferences.lastOpenedBudgetId.get() == id) {
      preferences.lastOpenedBudgetId.delete()
    }
  }
}
