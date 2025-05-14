package actual.budget.sync.vm

import actual.account.model.LoginToken
import actual.api.model.sync.UserFile
import actual.budget.model.BudgetId
import alakazam.kotlin.logging.Logger
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode.Immediate
import app.cash.molecule.launchMolecule
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okio.Path

@HiltViewModel(assistedFactory = SyncBudgetViewModel.Factory::class)
class SyncBudgetViewModel @AssistedInject constructor(
  @Assisted inputs: Inputs,
  private val budgetFileDownloader: BudgetFileDownloader,
  private val budgetInfoFetcher: BudgetInfoFetcher,
) : ViewModel() {
  private val token = inputs.token
  private val budgetId = inputs.budgetId

  private val mutableSteps = MutableStateFlow<PersistentMap<SyncStep, SyncStepState>>(
    persistentMapOf(
      SyncStep.FetchingFileInfo to SyncStepState.NotStarted,
      SyncStep.StartingDatabaseDownload to SyncStepState.NotStarted,
      SyncStep.DownloadingDatabase to SyncStepState.NotStarted,
      SyncStep.ValidatingDatabase to SyncStepState.NotStarted,
    ),
  )

  val stepStates: StateFlow<ImmutableMap<SyncStep, SyncStepState>> = mutableSteps.asStateFlow()

  val overallState: StateFlow<SyncOverallState> = viewModelScope.launchMolecule(Immediate) {
    val stepStates by mutableSteps.collectAsState()
    val states = stepStates.values
    when {
      states.all { it == SyncStepState.NotStarted } -> SyncOverallState.NotStarted
      states.all { it == SyncStepState.Succeeded } -> SyncOverallState.Succeeded
      states.any { it is SyncStepState.InProgress } -> SyncOverallState.InProgress
      states.any { it is SyncStepState.Failed } -> SyncOverallState.Failed
      else -> SyncOverallState.InProgress
    }
  }

  private var awaitJob: Job? = null

  init {
    Logger.d("init")
    start()
  }

  override fun onCleared() {
    super.onCleared()
    awaitJob?.cancel()
  }

  fun start() {
    Logger.d("start")
    awaitJob?.cancel()
    awaitJob = viewModelScope.launch {
      val userFileDeferred = async { fetchUserFileInfo() }
      val fileDownloadDeferred = async { downloadBudgetFileAsync() }
      val userFile = userFileDeferred.await()
      val downloadedPath = fileDownloadDeferred.await()

      if (userFile == null || downloadedPath == null) {
        Logger.w("Failed syncing?")
        return@launch
      } else {
        Logger.i("Succeeded syncing: $userFile and $downloadedPath")
      }

      // TODO: Actually load the data somewhere
      //      val buffer = if (userFile.encryptMeta != null) {
      //        decryptFile(syncState.path, userFile.encryptMeta)
      //      } else {
      //        syncState.path
      //      }
      //      importBuffer(buffer, userFile)
    }
  }

  private suspend fun CoroutineScope.fetchUserFileInfo(): UserFile? {
    setStepState(SyncStep.FetchingFileInfo, SyncStepState.InProgress.Indefinite)
    return when (val result = budgetInfoFetcher.fetch(token, budgetId)) {
      is BudgetInfoFetcher.Result.Failure -> {
        setStepState(SyncStep.FetchingFileInfo, SyncStepState.Failed(result.reason))
        null
      }

      is BudgetInfoFetcher.Result.Success -> {
        setStepState(SyncStep.FetchingFileInfo, SyncStepState.Succeeded)
        result.userFile
      }
    }
  }

  private suspend fun CoroutineScope.downloadBudgetFileAsync(): Path? {
    var downloadedDbPath: Path? = null
    setStepState(SyncStep.StartingDatabaseDownload, SyncStepState.InProgress.Indefinite)
    budgetFileDownloader.download(token, budgetId).collect { state ->
      setStepState(SyncStep.StartingDatabaseDownload, SyncStepState.Succeeded)
      val stepState = when (state) {
        is DownloadState.InProgress -> SyncStepState.InProgress.Definite(state.toPercent())
        is DownloadState.Failure -> SyncStepState.Failed(state.message)
        is DownloadState.Done -> {
          downloadedDbPath = state.path
          SyncStepState.Succeeded
        }
      }
      setStepState(SyncStep.DownloadingDatabase, stepState)
      Logger.d("stepState=%s", stepState)
    }
    return downloadedDbPath
  }

  private fun DownloadState.InProgress.toPercent() = Percent(read.numBytes, total.numBytes)

  private fun setStepState(step: SyncStep, state: SyncStepState) {
    mutableSteps.update { stepStates -> stepStates.put(step, state) }
  }

  data class Inputs(
    val token: LoginToken,
    val budgetId: BudgetId,
  )

  @AssistedFactory
  fun interface Factory {
    fun create(
      @Assisted inputs: Inputs,
    ): SyncBudgetViewModel
  }
}
