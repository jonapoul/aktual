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
import actual.test.testHttpClient
import actual.url.model.Protocol
import actual.url.model.ServerUrl
import alakazam.test.core.MainDispatcherRule
import alakazam.test.core.TestCoroutineContexts
import app.cash.turbine.test
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import okio.buffer
import okio.fakefilesystem.FakeFileSystem
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.net.NoRouteToHostException
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BudgetFileDownloaderTest {
  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()

  @get:Rule
  val temporaryFolder = TemporaryFolder()

  private lateinit var budgetFileDownloader: BudgetFileDownloader
  private lateinit var fileSystem: FakeFileSystem
  private lateinit var databaseDirectory: TestDatabaseDirectory
  private lateinit var apisStateHolder: ActualApisStateHolder
  private lateinit var mockEngine: MockEngine

  @After
  fun after() {
    mockEngine.close()
    fileSystem.checkNoOpenFiles()
  }

  @Before
  fun before() {
    fileSystem = FakeFileSystem()
    databaseDirectory = TestDatabaseDirectory(fileSystem)
    apisStateHolder = ActualApisStateHolder()

    budgetFileDownloader = BudgetFileDownloader(
      contexts = TestCoroutineContexts(mainDispatcherRule),
      databaseDirectory = databaseDirectory,
      apisStateHolder = apisStateHolder,
    )
  }

  @Test
  fun `Succeed downloading`() = runTest {
    // given
    val dataSize = 1024.bytes
    val data = ByteArray(dataSize.numBytes.toInt()) { (it % Byte.MAX_VALUE).toByte() }
    mockEngine = MockEngine {
      respond(data, headers = headersOf(HttpHeaders.ContentLength, dataSize.numBytes.toString()))
    }
    apisStateHolder.update { buildApis() }

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
      val path = databaseDirectory.pathFor(BUDGET_ID)
      assertTrue(fileSystem.exists(path))
      val downloadedData = fileSystem.source(path).buffer().use { it.readByteArray() }
      assertContentEquals(expected = data, actual = downloadedData)

      awaitComplete()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Handle network failure`() = runTest {
    // given the API call throws network exception
    mockEngine = MockEngine { throw NoRouteToHostException() }
    apisStateHolder.update { buildApis() }

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
        actual = fileSystem.allPaths.toList(),
        expected = emptyList(),
      )

      awaitComplete()
      cancelAndIgnoreRemainingEvents()
    }
  }

  private fun buildApis(
    syncApi: SyncApi = SyncApi(SERVER_URL, testHttpClient(mockEngine, ActualJson)),
  ) = mockk<ActualApis> { every { sync } returns syncApi }

  private companion object {
    val TOKEN = LoginToken("abc-123")
    val BUDGET_ID = BudgetId("xyz-789")
    val SERVER_URL = ServerUrl(Protocol.Https, "actual.website.com")
  }
}
