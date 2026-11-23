package aktual.budget.list.vm

import aktual.api.client.SyncApi
import aktual.api.model.sync.DeleteUserFileRequest
import aktual.api.model.sync.UserFile
import aktual.budget.list.vm.ListBudgetsState.Failure
import aktual.budget.list.vm.ListBudgetsState.Loading
import aktual.budget.list.vm.ListBudgetsState.Success
import aktual.budget.model.Budget
import aktual.budget.model.BudgetId
import aktual.budget.model.directoryId
import aktual.budget.utils.BudgetCreator
import aktual.budget.utils.BudgetFiles
import aktual.budget.utils.ImportResult
import aktual.budget.utils.UploadResult
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import aktual.core.utils.UuidGenerator
import aktual.core.utils.runCatchingCancellable
import aktual.di.LoggedInScope
import aktual.di.RunLevelController
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.log
import aktual.prefs.AppPreferences
import aktual.prefs.asStateFlow
import aktual.prefs.delete
import alakazam.kotlin.CoroutineContexts
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import java.io.InputStream
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.collections.immutable.toImmutableList
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

@Stable
@ViewModelKey
@ContributesIntoMap(LoggedInScope::class)
@Suppress("LongParameterList")
class ListBudgetsViewModel
internal constructor(
  private val token: Token,
  private val preferences: AppPreferences,
  private val budgetListFetcher: BudgetListFetcher,
  private val reconciler: BudgetReconciler,
  private val files: BudgetFiles,
  private val contexts: CoroutineContexts,
  private val syncApi: SyncApi,
  private val runLevels: RunLevelController,
  private val zipImporter: ZipBudgetImporter,
  private val budgetCreator: BudgetCreator,
  private val uuids: UuidGenerator,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  private val mutableEvent = MutableSharedFlow<ListBudgetsEvent>(extraBufferCapacity = 1)
  val event: Flow<ListBudgetsEvent> = mutableEvent.asSharedFlow()

  val serverUrl: StateFlow<ServerUrl?> = preferences.serverUrl.asStateFlow(viewModelScope)

  private val mutableState =
    MutableStateFlow<ListBudgetsState>(Loading(preferences.mostRecentNumBudgets.default))

  val state: StateFlow<ListBudgetsState> = mutableState.asStateFlow()

  private val mutableDeletingState = MutableStateFlow<DeletingState>(Inactive)
  val deletingState: StateFlow<DeletingState> = mutableDeletingState.asStateFlow()

  private val mutableCloseDialog = MutableSharedFlow<Boolean>()
  val closeDialog: SharedFlow<Boolean> = mutableCloseDialog.asSharedFlow()

  private val mutableImportState = MutableStateFlow<ImportBudgetState>(Idle)
  val importState: StateFlow<ImportBudgetState> = mutableImportState.asStateFlow()

  private val mutableCreateState = MutableStateFlow<CreateBudgetState>(Idle)
  val createState: StateFlow<CreateBudgetState> = mutableCreateState.asStateFlow()

  private var lastUserFiles: List<UserFile>? = null

  init {
    log.d { "init" }
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
    log.d { "retry" }
    fetchState()
  }

  fun openBudget(budget: Budget) {
    log.d { "openBudget $budget" }
    val id = budget.directoryId
    viewModelScope.launch {
      val metadata =
        if (budget.canOpenLocally()) withContext(contexts.io) { files.readMetadata(id) } else null

      if (metadata == null) {
        // Downloads by cloud file ID, so encrypted budgets get their key asked for
        val cloudId = (budget as? Cloud)?.cloudFileId ?: id
        mutableEvent.emit(ListBudgetsEvent.ShowSyncDialog(cloudId))
        return@launch
      }

      // Already downloaded, so open it straight away. Opening it syncs any changes since last time
      runLevels.onBudget(id, metadata)
      navToBudget(id)
    }
  }

  fun onSyncComplete(id: BudgetId) {
    viewModelScope.launch { navToBudget(id) }
  }

  private suspend fun navToBudget(id: BudgetId) {
    preferences.lastOpenedBudgetId.set(id)
    mutableEvent.emit(NavToBudget)
  }

  // Encrypted budgets without a stored key go through the full sync so the user gets asked for it.
  // Local-only budgets are already decrypted on disk, so they never need the key
  private fun Budget.canOpenLocally(): Boolean =
    when (this) {
      is Local -> true
      is Synced,
      is Unknown -> encryptKeyId == null || hasKey
      is Remote,
      is Detached,
      is Broken -> false
    }

  fun clearDeletingState() = mutableDeletingState.update { Inactive }

  fun deleteRemote(budget: Budget.Cloud) {
    val id = budget.directoryId
    log.d { "deleteRemote $budget" }
    mutableDeletingState.update { DeletingState.Active(deletingRemote = true) }

    viewModelScope.launch {
      with(files) {
        val budgetDir = directory(id)
        withContext(contexts.io) { fileSystem.deleteRecursively(budgetDir) }
      }
      clearLastOpenedIfMatches(id)

      runCatchingCancellable(
        attempt = {
          val request = DeleteUserFileRequest(budget.cloudFileId, token)
          syncApi.delete(request)
        },
        onFailure = { e -> log.e(e) { "Failed deleting $id" } },
      )

      log.i { "Successfully deleted $id" }
      clearDeletingState()
      mutableCloseDialog.emit(true)

      fetchState()
    }
  }

  fun deleteLocal(id: BudgetId) {
    log.d { "deleteLocal $id" }
    mutableDeletingState.update { DeletingState.Active(deletingLocal = true) }
    viewModelScope.launch {
      with(files) {
        val budgetDir = directory(id)
        if (fileSystem.exists(budgetDir)) {
          withContext(contexts.io) { fileSystem.deleteRecursively(budgetDir) }
        }

        log.d { "Successfully deleted $budgetDir" }
        clearLastOpenedIfMatches(id)
        clearDeletingState()
        mutableCloseDialog.emit(true)
        applyReconcile(lastUserFiles)
      }
    }
  }

  /** Imports a zip in Actual's export format as a new local budget, then opens it. */
  fun importZip(open: () -> InputStream) {
    if (mutableImportState.value is Importing) return
    mutableImportState.update { Importing }
    viewModelScope.launch {
      val (id, result) = zipImporter.import(open)
      log.i { "importZip $id: $result" }

      when (result) {
        is ImportResult.Success -> {
          mutableImportState.update { Idle }
          runLevels.onBudget(id, result.meta)
          navToBudget(id)
        }
        is ImportResult.Failure ->
          mutableImportState.update { ImportBudgetState.Failed(result.toImportFailure()) }
      }
    }
  }

  // Leaves a running import alone, so reopening the dialog shows its progress
  fun clearImportFailure() = mutableImportState.update { if (it is Failed) Idle else it }

  private fun ImportResult.Failure.toImportFailure(): ImportFailure =
    when (this) {
      NotZipFile -> NotZip
      InvalidZipFile -> MissingFiles
      InvalidMetaFile -> BadMetadata
      is OtherFailure -> Unknown
    }

  /** Creates a new budget, then opens it. */
  fun createBudget(name: String, syncWithServer: Boolean) {
    if (mutableCreateState.value is Creating) return
    mutableCreateState.update { Creating }
    viewModelScope.launch {
      val trimmed = name.trim()
      val id = budgetIdFromName(trimmed, uuids())
      val metadata = budgetCreator(id, trimmed)
      log.i { "createBudget $id syncWithServer=$syncWithServer: $metadata" }

      if (metadata == null) {
        mutableCreateState.update { Failed }
      } else {
        val graph = runLevels.onBudget(id, metadata)
        val result = if (syncWithServer) graph.uploader.upload() else UploadResult.Success
        log.i { "Upload $id: $result" }
        if (result is UploadResult.Failure) {
          // It stays local-only, like upstream, but tell the user before opening it
          mutableCreateState.update { CreateBudgetState.UploadFailed(id, result) }
        } else {
          mutableCreateState.update { Idle }
          navToBudget(id)
        }
      }
    }
  }

  // After a failed upload, the budget is already open
  fun openCreatedBudget() {
    val state = mutableCreateState.value as? UploadFailed ?: return
    mutableCreateState.update { Idle }
    viewModelScope.launch { navToBudget(state.id) }
  }

  // Leaves a running creation alone, so reopening the dialog shows its progress
  fun clearCreateFailure() = mutableCreateState.update {
    if (it is Failed) Idle else it
  }

  fun logOut() {
    viewModelScope.launch {
      preferences.token.delete()
      runLevels.onLoggedOut()
      mutableEvent.emit(LogOut)
    }
  }

  private fun fetchState() {
    viewModelScope.launch {
      val mostRecentNumBudgets = preferences.mostRecentNumBudgets.get()
      mutableState.update { Loading(mostRecentNumBudgets) }

      val result = budgetListFetcher.fetchBudgets(token)
      log.d { "Fetch budgets result = $result" }
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
