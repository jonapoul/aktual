package aktual.budget.sync.vm

import aktual.api.client.SyncApiImpl
import aktual.budget.model.BudgetFiles
import aktual.budget.model.BudgetId
import aktual.budget.sync.vm.DownloadState.Done
import aktual.budget.sync.vm.DownloadState.Failure
import aktual.budget.sync.vm.DownloadState.InProgress
import aktual.core.model.Protocol
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import aktual.core.model.bytes
import aktual.test.CoTemporaryFolder
import aktual.test.emptyMockEngine
import aktual.test.enqueueResponse
import aktual.test.existsOn
import aktual.test.testBudgetFiles
import aktual.test.testHttpClient
import alakazam.test.TestCoroutineContexts
import alakazam.test.unconfinedDispatcher
import app.cash.burst.InterceptTest
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode
import java.net.NoRouteToHostException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path

class BudgetFileDownloaderTest {
  @InterceptTest val temporaryFolder = CoTemporaryFolder()

  private lateinit var budgetFileDownloader: BudgetFileDownloader
  private lateinit var budgetFiles: BudgetFiles
  private lateinit var mockEngine: MockEngine.Queue

  @BeforeTest
  fun before() {
    mockEngine = emptyMockEngine()
  }

  fun TestScope.before(
    syncApi: SyncApiImpl =
      SyncApiImpl(
        serverUrl = SERVER_URL,
        client = testHttpClient(mockEngine),
        fileSystem = FileSystem.SYSTEM,
      )
  ) {
    budgetFiles = testBudgetFiles(temporaryFolder)
    budgetFileDownloader =
      BudgetFileDownloader(
        contexts = TestCoroutineContexts(unconfinedDispatcher),
        budgetFiles = budgetFiles,
        syncApi = syncApi,
        token = TOKEN,
      )
  }

  @AfterTest
  fun after() {
    mockEngine.close()
  }

  @Test
  fun `Succeed downloading`() = runTest {
    // given
    before()
    val dataSize = 1024.bytes
    val data = ByteArray(dataSize.numBytes.toInt()) { (it % Byte.MAX_VALUE).toByte() }
    mockEngine.enqueueResponse(data)

    // when
    budgetFileDownloader.download(BUDGET_ID).test {
      // Then progress state is emitted
      var state = awaitItem()
      while (state !is Done) {
        assertThat(state).isInstanceOf<InProgress>()
        state = awaitItem()
      }

      // and the final state is done
      val finalState = state
      assertThat(finalState).isInstanceOf<Done>()
      assertThat(finalState.total).isEqualTo(dataSize)

      // and it contains all our data, nothing more or less
      val path = budgetFiles.encryptedZip(BUDGET_ID)
      assertThat(path).existsOn(FileSystem.SYSTEM)
      val downloadedData = FileSystem.SYSTEM.read(path) { readByteArray() }
      assertThat(downloadedData).isEqualTo(data)

      awaitComplete()
    }
  }

  @Test
  fun `Handle network failure`() = runTest {
    // given the API call throws network exception
    before()
    mockEngine += { throw NoRouteToHostException() }

    // when
    budgetFileDownloader.download(BUDGET_ID).test {
      // Then progress state is emitted
      var state = awaitItem()
      while (state is InProgress) {
        state = awaitItem()
      }

      // and the final state is a network failure
      assertThat(state).isInstanceOf<Failure.IO>()

      // and only the temp dir was created
      assertThat(temporaryFolder.list().map(Path::toFile))
        .isEqualTo(listOf(budgetFiles.tmp().toFile()))

      awaitComplete()
    }
  }

  @Test
  fun `Handle HTTP failure`() = runTest {
    // given the API call returns error code
    before()
    mockEngine += { respondError(HttpStatusCode.NotFound) }

    // when
    budgetFileDownloader.download(BUDGET_ID).test {
      // Then in progress momentarily
      var state = awaitItem()
      while (state is InProgress) {
        state = awaitItem()
      }

      // Then HTTP state is the result
      assertThat(state).isInstanceOf<Failure.Http>()
      awaitComplete()
    }

    // and only the temp dir was created
    assertThat(temporaryFolder.list().map(Path::toFile))
      .isEqualTo(listOf(budgetFiles.tmp().toFile()))
  }

  private companion object {
    val TOKEN = Token("abc-123")
    val BUDGET_ID = BudgetId("xyz-789")
    val SERVER_URL = ServerUrl(Protocol.Https, "actual.website.com")
  }
}
