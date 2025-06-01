package actual.budget.sync.vm

import actual.account.model.LoginToken
import actual.api.model.sync.UserFile
import actual.budget.model.BudgetId
import actual.budget.sync.vm.SyncStep.DownloadingDatabase
import actual.budget.sync.vm.SyncStep.FetchingFileInfo
import actual.budget.sync.vm.SyncStep.ValidatingDatabase
import actual.core.files.BudgetFiles
import actual.core.files.decryptedZip
import actual.core.model.Percent
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
  private val fileDownloader: BudgetFileDownloader,
  private val infoFetcher: BudgetInfoFetcher,
  private val decrypter: DatabaseDecrypter,
  private val importer: DatabaseImporter,
  private val files: BudgetFiles,
) : ViewModel() {
  private val token = inputs.token
  private val budgetId = inputs.budgetId

  private val mutableSteps = MutableStateFlow<PersistentMap<SyncStep, SyncStepState>>(
    persistentMapOf(
      FetchingFileInfo to SyncStepState.NotStarted,
      DownloadingDatabase to SyncStepState.NotStarted,
      ValidatingDatabase to SyncStepState.NotStarted,
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

  private var job: Job? = null

  init {
    Logger.d("init")
    start()
  }

  override fun onCleared() {
    super.onCleared()
    job?.cancel()
  }

  fun start() {
    Logger.d("start")
    job?.cancel()
    job = viewModelScope.launch {
      val userFileDeferred = async { fetchUserFileInfo() }
      val fileDownloadDeferred = async { downloadBudgetFileAsync() }
      val userFile = userFileDeferred.await()
      val downloadedPath = fileDownloadDeferred.await()

      if (userFile == null || downloadedPath == null) {
        Logger.w("Failed syncing?")
        return@launch
      }

      setStepState(ValidatingDatabase, SyncStepState.InProgress.Indefinite)

      Logger.i("Succeeded syncing: $userFile and $downloadedPath")
      val meta = userFile.encryptMeta
      val decryptResult = if (meta != null) {
        // Encrypted payload, so decrypt it and return the decrypted zip's path
        decrypter(userFile.fileId, downloadedPath, meta)
      } else {
        // Unencrypted, so just copy the payload to the expected place
        val targetPath = files.decryptedZip(budgetId)
        files.fileSystem.copy(source = downloadedPath, target = targetPath)
        DecryptResult.NotNeeded(targetPath)
      }
      Logger.i("decryptResult=$decryptResult")

      when (decryptResult) {
        is DecryptResult.Failure -> handleDecryptFailure(decryptResult)
        is DecryptResult.Success -> importDatabase(decryptResult.path, userFile)
      }
    }
  }

  private suspend fun CoroutineScope.fetchUserFileInfo(): UserFile? {
    setStepState(FetchingFileInfo, SyncStepState.InProgress.Indefinite)
    return when (val result = infoFetcher.fetch(token, budgetId)) {
      is BudgetInfoFetcher.Result.Failure -> {
        setStepState(FetchingFileInfo, SyncStepState.Failed(result.reason))
        null
      }

      is BudgetInfoFetcher.Result.Success -> {
        setStepState(FetchingFileInfo, SyncStepState.Succeeded)
        result.userFile
      }
    }
  }

  private suspend fun CoroutineScope.downloadBudgetFileAsync(): Path? {
    var downloadedDbPath: Path? = null
    setStepState(DownloadingDatabase, SyncStepState.InProgress.Indefinite)
    fileDownloader.download(token, budgetId).collect { state ->
      val stepState = when (state) {
        is DownloadState.InProgress -> SyncStepState.InProgress.Definite(state.toPercent())
        is DownloadState.Failure -> SyncStepState.Failed(state.message)
        is DownloadState.Done -> {
          downloadedDbPath = state.path
          SyncStepState.Succeeded
        }
      }
      setStepState(DownloadingDatabase, stepState)
      Logger.d("stepState=%s", stepState)
    }
    return downloadedDbPath
  }

  private fun DownloadState.InProgress.toPercent() = Percent(read.numBytes, total.numBytes)

  private fun setStepState(step: SyncStep, state: SyncStepState) {
    mutableSteps.update { stepStates -> stepStates.put(step, state) }
  }

  private fun handleDecryptFailure(result: DecryptResult.Failure) {
    val message = when (result) {
      DecryptResult.MissingKey -> "Missing key"
      is DecryptResult.UnknownAlgorithm -> "Unknown algorithm: ${result.algorithm}"
      is DecryptResult.OtherFailure -> "Other failure: ${result.message}"
    }
    setStepState(ValidatingDatabase, SyncStepState.Failed(message))
  }

  private suspend fun importDatabase(path: Path, userFile: UserFile) {
    val importResult = importer(userFile, path)
    Logger.i("importResult=$importResult")

    when (importResult) {
      is ImportResult.Failure -> setStepState(ValidatingDatabase, SyncStepState.Failed(importResult.toString()))
      is ImportResult.Success -> setStepState(ValidatingDatabase, SyncStepState.Succeeded)
    }
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
