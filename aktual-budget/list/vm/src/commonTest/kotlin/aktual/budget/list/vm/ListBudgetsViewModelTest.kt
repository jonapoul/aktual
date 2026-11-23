package aktual.budget.list.vm

import aktual.api.client.SyncApi
import aktual.api.model.sync.DeleteUserFileRequest
import aktual.api.model.sync.DeleteUserFileResponse
import aktual.api.model.sync.ListUserFilesResponse
import aktual.api.model.sync.UserFile
import aktual.budget.model.Budget
import aktual.budget.model.BudgetId
import aktual.budget.model.DbMetadata
import aktual.budget.model.directoryId
import aktual.budget.utils.BudgetCreator
import aktual.budget.utils.BudgetFiles
import aktual.budget.utils.BudgetUploader
import aktual.budget.utils.UploadResult
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import aktual.core.utils.UuidGenerator
import aktual.di.AktualGraph
import aktual.di.BudgetGraph
import aktual.di.RunLevelController
import aktual.prefs.AppPreferences
import aktual.prefs.AppPreferencesImpl
import aktual.test.TemporaryFolder
import aktual.test.buildPreferences
import aktual.test.testBudgetFiles
import aktual.test.viewModelContext
import alakazam.test.TestCoroutineContexts
import alakazam.test.standardDispatcher
import alakazam.test.unconfinedDispatcher
import androidx.lifecycle.viewModelScope
import app.cash.burst.InterceptTest
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import app.cash.turbine.turbineScope
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

class ListBudgetsViewModelTest {
  @InterceptTest val temporaryFolder = TemporaryFolder()

  private lateinit var files: BudgetFiles
  private lateinit var preferences: AppPreferences
  private val syncApi = mockk<SyncApi>()
  private var userFiles = emptyList<UserFile>()
  private var uploadResult: UploadResult = Success
  private var uploads = 0
  private val opened = mutableListOf<BudgetId>()

  private val graph =
    mockk<BudgetGraph> {
      every { uploader } returns BudgetUploader { uploadResult.also { uploads++ } }
    }

  private val runLevels =
    object : RunLevelController {
      override fun onBudget(id: BudgetId, metadata: DbMetadata): BudgetGraph {
        opened += id
        return graph
      }

      override fun init(graphs: List<AktualGraph>) = error("Unused")

      override fun onServerChosen(url: ServerUrl) = error("Unused")

      override fun onLoggedIn(token: Token) = error("Unused")

      override fun onOfflineBudget(id: BudgetId, metadata: DbMetadata) = error("Unused")

      override fun onBudgetClosed() = error("Unused")

      override fun onLoggedOut() = error("Unused")

      override fun onServerCleared() = error("Unused")

      override fun close() = error("Unused")
    }

  private fun runVmTest(action: suspend TestScope.(ListBudgetsViewModel) -> Unit) = runTest {
    files = testBudgetFiles(temporaryFolder)
    preferences = AppPreferencesImpl(buildPreferences(unconfinedDispatcher))
    coEvery { syncApi.fetchUserFiles(any()) } answers { ListUserFilesResponse.Success(userFiles) }
    coEvery { syncApi.delete(any()) } returns DeleteUserFileResponse.Success("ok")

    val viewModel = viewModel()
    try {
      action(viewModel)
    } finally {
      // Stops the reconcile loop
      viewModel.viewModelScope.cancel()
    }
  }

  @Test
  fun `Create a local-only budget without uploading`() = runVmTest { viewModel ->
    viewModel.event.test {
      viewModel.createBudget("Household", syncWithServer = false)
      runCurrent()
      assertThat(awaitItem()).isEqualTo(NavToBudget)
    }

    assertThat(uploads).isEqualTo(0)
    assertThat(opened).containsExactly(NEW_ID)
  }

  @Test
  fun `Upload a synced budget before opening it`() = runVmTest { viewModel ->
    viewModel.event.test {
      viewModel.createBudget("Household", syncWithServer = true)
      runCurrent()
      assertThat(awaitItem()).isEqualTo(NavToBudget)
    }

    assertThat(uploads).isEqualTo(1)
    assertThat(opened).containsExactly(NEW_ID)
  }

  @Test
  fun `Tell the user about a failed upload before opening the budget`() = runVmTest { viewModel ->
    uploadResult = Unreachable

    turbineScope {
      val events = viewModel.event.testIn(backgroundScope)
      val states = viewModel.createState.testIn(backgroundScope)
      assertThat(states.awaitItem()).isEqualTo(Idle)

      viewModel.createBudget("Household", syncWithServer = true)
      runCurrent()
      assertThat(states.awaitItem()).isEqualTo(Creating)
      assertThat(states.awaitItem())
        .isEqualTo(CreateBudgetState.UploadFailed(NEW_ID, UploadResult.Unreachable))
      events.expectNoEvents()

      viewModel.openCreatedBudget()
      runCurrent()
      assertThat(states.awaitItem()).isEqualTo(Idle)
      assertThat(events.awaitItem()).isEqualTo(NavToBudget)
    }

    assertThat(preferences.lastOpenedBudgetId.get()).isEqualTo(NEW_ID)
  }

  @Test
  fun `Open an uploaded budget from its own directory`() = runVmTest { viewModel ->
    seedUploaded()

    viewModel.event.test {
      viewModel.openBudget(synced())
      runCurrent()
      assertThat(awaitItem()).isEqualTo(NavToBudget)
    }

    assertThat(opened).containsExactly(LOCAL_ID)
  }

  @Test
  fun `Encrypted budget without a key downloads by cloud file ID`() = runVmTest { viewModel ->
    seedUploaded()

    viewModel.event.test {
      viewModel.openBudget(synced(encryptKeyId = "key"))
      runCurrent()
      assertThat(awaitItem()).isEqualTo(ListBudgetsEvent.ShowSyncDialog(CLOUD_ID))
    }

    assertThat(opened).isEmpty()
  }

  @Test
  fun `Delete the local copy of an uploaded budget`() = runVmTest { viewModel ->
    seedUploaded()

    viewModel.state.test {
      assertThat(awaitSuccess()).containsExactly(synced())

      // What the delete dialog passes
      viewModel.deleteLocal(synced().directoryId)
      runCurrent()
      assertThat(awaitSuccess()).containsExactly(remote())
    }
  }

  @Test
  fun `Delete an uploaded budget from the server and its own directory`() = runVmTest { viewModel ->
    seedUploaded()

    viewModel.deleteRemote(synced())
    runCurrent()

    assertThat(files.fileSystem.exists(files.directory(LOCAL_ID))).isFalse()
    coVerify { syncApi.delete(DeleteUserFileRequest(CLOUD_ID, TOKEN)) }
  }

  private fun seedUploaded() {
    files.writeMetadata(
      LOCAL_ID,
      DbMetadata(budgetName = "Household", cloudFileId = CLOUD_ID, groupId = GROUP_ID),
    )
    userFiles =
      listOf(UserFile(deleted = 0, fileId = CLOUD_ID, groupId = GROUP_ID, name = "Household"))
  }

  private fun synced(encryptKeyId: String? = null) =
    Budget.Synced(
      name = "Household",
      hasKey = false,
      encryptKeyId = encryptKeyId,
      cloudFileId = CLOUD_ID,
      groupId = GROUP_ID,
      owner = null,
      localId = LOCAL_ID,
    )

  private fun remote() =
    Budget.Remote(
      name = "Household",
      hasKey = false,
      encryptKeyId = null,
      cloudFileId = CLOUD_ID,
      groupId = GROUP_ID,
      owner = null,
    )

  private suspend fun ReceiveTurbine<ListBudgetsState>.awaitSuccess(): List<Budget> {
    while (true) {
      val item = awaitItem()
      if (item is Success) return item.budgets
    }
  }

  private fun TestScope.viewModel(): ListBudgetsViewModel {
    val contexts = TestCoroutineContexts(standardDispatcher)
    val uuids = UuidGenerator { "0123456789" }
    return ListBudgetsViewModel(
      token = TOKEN,
      preferences = preferences,
      budgetListFetcher = BudgetListFetcher(syncApi, contexts),
      reconciler =
        BudgetReconciler(
          files = files,
          keyPreferences = mockk { coEvery { contains(any()) } returns false },
          contexts = contexts,
        ),
      files = files,
      contexts = contexts,
      syncApi = syncApi,
      runLevels = runLevels,
      zipImporter = ZipBudgetImporter(mockk(), files, uuids, contexts),
      budgetCreator = BudgetCreator { _, name -> DbMetadata(budgetName = name) },
      uuids = uuids,
      viewModelContext = viewModelContext,
    )
  }

  private companion object {
    val TOKEN = Token("token")
    val NEW_ID = BudgetId("Household-0123456")
    val LOCAL_ID = BudgetId("Household-abcdefg")
    val CLOUD_ID = BudgetId("0f8b1c2e-3d4a-4b5c-9e6f-0a1b2c3d4e5f")
    const val GROUP_ID = "group"
  }
}
