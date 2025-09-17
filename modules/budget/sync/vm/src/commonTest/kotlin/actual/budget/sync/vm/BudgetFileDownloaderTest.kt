package actual.budget.sync.vm

import actual.api.client.ActualApis
import actual.api.client.ActualApisStateHolder
import actual.api.client.ActualJson
import actual.api.client.SyncDownloadApi
import actual.budget.model.BudgetId
import actual.budget.model.encryptedZip
import actual.budget.sync.vm.DownloadState.Done
import actual.budget.sync.vm.DownloadState.Failure
import actual.budget.sync.vm.DownloadState.InProgress
import actual.core.model.LoginToken
import actual.core.model.Protocol
import actual.core.model.ServerUrl
import actual.core.model.bytes
import actual.test.CoTemporaryFolder
import actual.test.TestBudgetFiles
import actual.test.emptyMockEngine
import actual.test.enqueueResponse
import actual.test.testHttpClient
import alakazam.test.core.TestCoroutineContexts
import alakazam.test.core.unconfinedDispatcher
import app.cash.burst.InterceptTest
import app.cash.turbine.test
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path
import java.net.NoRouteToHostException
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BudgetFileDownloaderTest {
  @InterceptTest val temporaryFolder = CoTemporaryFolder()

  private lateinit var budgetFileDownloader: BudgetFileDownloader
  private lateinit var budgetFiles: TestBudgetFiles
  private lateinit var apisStateHolder: ActualApisStateHolder
  private lateinit var mockEngine: MockEngine.Queue
  private lateinit var fileSystem: FileSystem

  fun TestScope.before() {
    fileSystem = FileSystem.SYSTEM
    budgetFiles = TestBudgetFiles(temporaryFolder)
    mockEngine = emptyMockEngine()

    val syncDownloadApi = SyncDownloadApi(
      serverUrl = SERVER_URL,
      client = testHttpClient(mockEngine, ActualJson),
      fileSystem = fileSystem,
    )

    apisStateHolder = ActualApisStateHolder()
    apisStateHolder.update {
      mockk<ActualApis>(relaxed = true) {
        every { syncDownload } returns syncDownloadApi
      }
    }

    budgetFileDownloader = BudgetFileDownloader(
      contexts = TestCoroutineContexts(unconfinedDispatcher),
      budgetFiles = budgetFiles,
      apisStateHolder = apisStateHolder,
    )
  }

  @AfterTest
  fun after() {
    mockEngine.close()
  }

  @Test
  fun `Fail if no APIs`() = runTest {
    // given
    before()
    apisStateHolder.reset()

    // when
    budgetFileDownloader.download(TOKEN, BUDGET_ID).test {
      // then
      assertEquals(expected = Failure.NotLoggedIn, actual = awaitItem())
      awaitComplete()
    }
  }

  @Test
  fun `Succeed downloading`() = runTest {
    // given
    before()
    val dataSize = 1024.bytes
    val data = ByteArray(dataSize.numBytes.toInt()) { (it % Byte.MAX_VALUE).toByte() }
    mockEngine.enqueueResponse(data)

    // when
    budgetFileDownloader.download(TOKEN, BUDGET_ID).test {
      // Then progress state is emitted
      var state = awaitItem()
      while (state !is Done) {
        assertIs<InProgress>(state, "Should be in progress, got $state")
        state = awaitItem()
      }

      // and the final state is done
      val finalState = state
      assertIs<Done>(finalState)
      assertEquals(expected = dataSize, actual = finalState.total)

      // and it contains all our data, nothing more or less
      val path = budgetFiles.encryptedZip(BUDGET_ID)
      assertTrue(fileSystem.exists(path))
      val downloadedData = fileSystem.read(path) { readByteArray() }
      assertContentEquals(expected = data, actual = downloadedData)

      awaitComplete()
    }
  }

  @Test
  fun `Handle network failure`() = runTest {
    // given the API call throws network exception
    before()
    mockEngine += { throw NoRouteToHostException() }

    // when
    budgetFileDownloader.download(TOKEN, BUDGET_ID).test {
      // Then progress state is emitted
      var state = awaitItem()
      while (state is InProgress) {
        state = awaitItem()
      }

      // and the final state is a network failure
      assertIs<Failure.IO>(state)

      // and only the temp dir was created
      assertEquals(
        actual = temporaryFolder.list().map(Path::toFile),
        expected = listOf(budgetFiles.tmp().toFile()),
      )

      awaitComplete()
    }
  }

  @Test
  fun `Handle HTTP failure`() = runTest {
    // given the API call returns error code
    before()
    mockEngine += { respondError(HttpStatusCode.NotFound) }

    // when
    budgetFileDownloader.download(TOKEN, BUDGET_ID).test {
      // Then in progress momentarily
      var state = awaitItem()
      while (state is InProgress) {
        state = awaitItem()
      }

      // Then HTTP state is emitted
      val httpState = state
      assertIs<Failure.Http>(httpState)
      awaitComplete()
    }

    // and only the temp dir was created
    assertEquals(
      actual = temporaryFolder.list().map(Path::toFile),
      expected = listOf(budgetFiles.tmp().toFile()),
    )
  }

  private companion object {
    val TOKEN = LoginToken("abc-123")
    val BUDGET_ID = BudgetId("xyz-789")
    val SERVER_URL = ServerUrl(Protocol.Https, "actual.website.com")
  }
}
