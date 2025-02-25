package actual.budget.sync.vm

import actual.account.model.LoginToken
import actual.api.client.ActualApis
import actual.api.client.ActualApisStateHolder
import actual.api.client.ActualJson
import actual.api.client.SyncApi
import actual.budget.model.BudgetId
import actual.budget.sync.vm.DownloadState.Done
import actual.budget.sync.vm.DownloadState.Failed
import actual.budget.sync.vm.DownloadState.InProgress
import actual.test.MockWebServerRule
import alakazam.kotlin.core.EmptyLogger
import alakazam.test.core.MainDispatcherRule
import alakazam.test.core.TestCoroutineContexts
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.net.NoRouteToHostException
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs

class BudgetFileDownloaderTest {
  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()

  @get:Rule
  val temporaryFolder = TemporaryFolder()

  @get:Rule
  val webServerRule = MockWebServerRule()

  private lateinit var budgetFileDownloader: BudgetFileDownloader
  private lateinit var fileStore: FileStore
  private lateinit var apisStateHolder: ActualApisStateHolder

  @Before
  fun before() {
    fileStore = TestFileStore(temporaryFolder)
    apisStateHolder = ActualApisStateHolder()

    budgetFileDownloader = BudgetFileDownloader(
      contexts = TestCoroutineContexts(mainDispatcherRule),
      fileStore = fileStore,
      apisStateHolder = apisStateHolder,
    )
  }

  @Test
  fun `Succeed downloading`() = runTest {
    // given
    apisStateHolder.update { buildApis() }
    val dataSize = 1024.bytes
    val data = ByteArray(dataSize.numBytes.toInt()) { (it % Byte.MAX_VALUE).toByte() }
    webServerRule.enqueue(data, code = 200)

    // when
    budgetFileDownloader.download(TOKEN, BUDGET_ID).test {
      // Then progress state is emitted
      var state = awaitItem()
      while (state !is Done) {
        assertIs<InProgress>(state)
        state = awaitItem()
      }

      // and the final state is done
      val finalState = state
      assertIs<Done>(finalState)
      assertEquals(expected = dataSize, actual = finalState.total)

      // and it contains all our data, nothing more or less
      val downloadedData = finalState.file.readBytes()
      assertContentEquals(expected = data, actual = downloadedData)

      awaitComplete()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Handle network failure`() = runTest {
    // given the API call throws network exception
    val syncApi = mockk<SyncApi> {
      coEvery { downloadUserFile(any(), any()) } throws NoRouteToHostException()
    }
    apisStateHolder.update { buildApis(syncApi) }

    // when
    budgetFileDownloader.download(TOKEN, BUDGET_ID).test {
      // Then progress state is emitted
      var state = awaitItem()
      while (state is InProgress) {
        state = awaitItem()
      }

      // and the final state is a network failure
      assertIs<Failed>(state)

      // and no files were created
      assertContentEquals(
        actual = temporaryFolder.root
          .listFiles()
          .orEmpty()
          .toList(),
        expected = emptyList(),
      )

      awaitComplete()
      cancelAndIgnoreRemainingEvents()
    }
  }

  private fun buildApis(
    syncApi: SyncApi = webServerRule.buildApi(ActualJson),
  ) = mockk<ActualApis> { every { sync } returns syncApi }

  private companion object {
    val TOKEN = LoginToken("abc-123")
    val BUDGET_ID = BudgetId("xyz-789")
  }
}
