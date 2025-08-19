package actual.budget.sync.vm

import actual.account.model.LoginToken
import actual.account.model.Password
import actual.api.model.sync.EncryptMeta
import actual.api.model.sync.UserFile
import actual.budget.encryption.KeyGenerator
import actual.budget.model.BudgetFiles
import actual.budget.model.BudgetId
import actual.budget.model.decryptedZip
import actual.budget.model.encryptedZip
import actual.budget.sync.vm.SyncStep.DownloadingDatabase
import actual.budget.sync.vm.SyncStep.FetchingFileInfo
import actual.budget.sync.vm.SyncStep.ValidatingDatabase
import actual.core.di.AssistedFactoryKey
import actual.core.di.BudgetGraphHolder
import actual.core.di.ViewModelAssistedFactory
import actual.core.di.ViewModelScope
import actual.core.model.Percent
import actual.core.model.UrlOpener
import actual.prefs.KeyPreferences
import alakazam.kotlin.core.launchInfiniteLoop
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode.Immediate
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat
import okio.Path
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Suppress("LongParameterList", "ComplexCondition", "UnusedPrivateProperty")
@Inject
class SyncBudgetViewModel(
  @Assisted private val token: LoginToken,
  @Assisted private val budgetId: BudgetId,
  private val fileDownloader: BudgetFileDownloader,
  private val infoFetcher: BudgetInfoFetcher,
  private val decrypter: Decrypter,
  private val importer: DatabaseImporter,
  private val files: BudgetFiles,
  private val urlOpener: UrlOpener,
  private val keyGenerator: KeyGenerator,
  private val keyFetcher: KeyFetcher,
  private val keyPreferences: KeyPreferences,
  private val budgetGraphs: BudgetGraphHolder,
) : ViewModel() {
  private var cachedData: CachedEncryptedData? = null

  private val mutablePasswordState = MutableStateFlow<KeyPasswordState>(KeyPasswordState.Inactive)
  val passwordState: StateFlow<KeyPasswordState> = mutablePasswordState.asStateFlow()

  private val mutableSteps = MutableStateFlow<PersistentMap<SyncStep, SyncStepState>>(defaultStates())
  val stepStates: StateFlow<ImmutableMap<SyncStep, SyncStepState>> = mutableSteps.asStateFlow()

  val budgetIsLoaded: StateFlow<Boolean> = budgetGraphs
    .map { it?.budgetId == budgetId }
    .stateIn(viewModelScope, Eagerly, initialValue = false)

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

  private var syncJob: Job? = null
  private var logStatesJob: Job? = null

  init {
    logcat.d { "init" }
    start()
  }

  override fun onCleared() {
    super.onCleared()
    syncJob?.cancel()
    logStatesJob?.cancel()

    with(files) {
      fileSystem.delete(encryptedZip(budgetId))
      fileSystem.delete(decryptedZip(budgetId))
    }
  }

  fun clearBudget() {
    logcat.v { "clearBudget" }
    budgetGraphs.clear()
  }

  fun enterKeyPassword(input: Password) = mutablePasswordState.update { KeyPasswordState.Active(input) }

  fun dismissKeyPasswordDialog() = mutablePasswordState.update { KeyPasswordState.Inactive }

  fun learnMore() {
    logcat.v { "learnMore" }
    urlOpener(LEARN_MORE_URL)
  }

  fun confirmKeyPassword() {
    logcat.v { "confirmKeyPassword" }
    val state = mutablePasswordState.value
    dismissKeyPasswordDialog()
    val cachedData = cachedData
    val meta = cachedData?.meta
    val keyId = meta?.keyId
    if (state !is KeyPasswordState.Active || cachedData == null || meta == null || keyId == null) {
      error("Should never happen? state=$state, cachedData=$cachedData, meta=$meta, keyId=$keyId")
    }

    setStepState(ValidatingDatabase, SyncStepState.InProgress.Indefinite)
    viewModelScope.launch {
      val fetched = keyFetcher(budgetId, token, state.input)
      when (fetched) {
        is FetchKeyResult.Failure -> logcat.w { "Failed fetching keys: $fetched" }

        is FetchKeyResult.Success -> {
          // val key = keyGenerator(fetched.key)
          keyPreferences[keyId] = fetched.key

          val result = decrypter(budgetId, meta, cachedData.encryptedPath)
          handleDecryptResult(result, cachedData.encryptedPath, meta, cachedData.userFile)
        }
      }
    }
  }

  fun start() {
    logcat.d { "start" }
    syncJob?.cancel()
    logStatesJob?.cancel()
    mutableSteps.update { defaultStates() }
    dismissKeyPasswordDialog()

    logStatesJob = viewModelScope.launchInfiniteLoop {
      logStates()
      delay(duration = 0.1.seconds)
    }

    syncJob = viewModelScope.launch {
      val userFileDeferred = async { fetchUserFileInfo() }
      val fileDownloadDeferred = async { downloadBudgetFileAsync() }
      val userFile = userFileDeferred.await()
      val downloadedPath = fileDownloadDeferred.await()

      logStatesJob?.cancel()
      logStates() // make sure we log the final state

      if (userFile == null || downloadedPath == null) {
        logcat.w { "Failed syncing?" }
        return@launch
      }

      setStepState(ValidatingDatabase, SyncStepState.InProgress.Indefinite)

      logcat.i { "Succeeded syncing: $userFile and $downloadedPath" }
      val meta = userFile.encryptMeta
      val decryptResult = if (meta != null) {
        // Encrypted payload, so decrypt it and return the decrypted zip's path
        decrypter(userFile.fileId, meta, downloadedPath)
      } else {
        // Unencrypted, so just copy the payload to the expected place
        val targetPath = files.decryptedZip(budgetId)
        files.fileSystem.copy(source = downloadedPath, target = targetPath)
        DecryptResult.NotNeeded(targetPath)
      }
      handleDecryptResult(decryptResult, downloadedPath, meta, userFile)
    }
  }

  private fun logStates() {
    val stateMap = mutableSteps.value
    logcat.v { "stepState=$stateMap" }
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
    }
    return downloadedDbPath
  }

  private fun DownloadState.InProgress.toPercent() = Percent(read.numBytes, total.numBytes)

  private fun setStepState(step: SyncStep, state: SyncStepState) {
    mutableSteps.update { stepStates -> stepStates.put(step, state) }
  }

  private suspend fun handleDecryptResult(
    result: DecryptResult,
    encryptedPath: Path,
    meta: EncryptMeta?,
    userFile: UserFile,
  ) {
    logcat.i { "decryptResult=$result" }
    when (result) {
      is DecryptResult.Failure -> handleDecryptFailure(result, encryptedPath, userFile, meta)
      is DecryptResult.DecryptedFile -> importDatabase(result.path, userFile)
      is DecryptResult.NotNeeded -> importDatabase(result.path, userFile)
      is DecryptResult.DecryptedBuffer -> error("Should never happen!")
    }
  }

  private fun handleDecryptFailure(
    result: DecryptResult.Failure,
    encryptedPath: Path,
    userFile: UserFile,
    meta: EncryptMeta?,
  ) {
    val message = when (result) {
      is DecryptResult.UnknownAlgorithm -> "Unknown algorithm: ${result.algorithm}"

      is DecryptResult.OtherFailure -> "Other failure: ${result.message}"

      DecryptResult.MissingKey -> {
        cachedData = CachedEncryptedData(encryptedPath, userFile, meta)

        // wait a little before showing the dialog
        viewModelScope.launch {
          delay(500.milliseconds)
          mutablePasswordState.update { KeyPasswordState.Active(Password.Empty) }
        }

        "Missing key"
      }
    }
    setStepState(ValidatingDatabase, SyncStepState.Failed(message))
  }

  private suspend fun importDatabase(path: Path, userFile: UserFile) {
    val result = importer(userFile, path)
    logcat.i { "importResult=$result" }

    when (result) {
      is ImportResult.Failure -> {
        setStepState(ValidatingDatabase, SyncStepState.Failed(result.toString()))
      }

      is ImportResult.Success -> {
        val budgetGraph = budgetGraphs.update(result.meta)
        logcat.i { "Built new budget component from $budgetId: $budgetGraph" }
        setStepState(ValidatingDatabase, SyncStepState.Succeeded)
      }
    }
  }

  private data class CachedEncryptedData(
    val encryptedPath: Path,
    val userFile: UserFile,
    val meta: EncryptMeta?,
  )

  @AssistedFactory
  @AssistedFactoryKey(Factory::class)
  @ContributesIntoMap(ViewModelScope::class)
  fun interface Factory : ViewModelAssistedFactory {
    fun create(
      @Assisted token: LoginToken,
      @Assisted budgetId: BudgetId,
    ): SyncBudgetViewModel
  }

  private companion object {
    const val LEARN_MORE_URL = "https://actualbudget.org/docs/getting-started/sync/#end-to-end-encryption"

    fun defaultStates() = persistentMapOf(
      FetchingFileInfo to SyncStepState.NotStarted,
      DownloadingDatabase to SyncStepState.NotStarted,
      ValidatingDatabase to SyncStepState.NotStarted,
    )
  }
}
