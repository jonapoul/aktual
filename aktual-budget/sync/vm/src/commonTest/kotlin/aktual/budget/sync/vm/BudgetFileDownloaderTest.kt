package aktual.budget.sync.vm

import aktual.api.client.AktualApis
import aktual.api.client.AktualApisStateHolder
import aktual.api.client.AktualJson
import aktual.api.client.SyncDownloadApi
import aktual.budget.model.BudgetId
import aktual.budget.model.encryptedZip
import aktual.budget.sync.vm.DownloadState.Done
import aktual.budget.sync.vm.DownloadState.Failure
import aktual.budget.sync.vm.DownloadState.InProgress
import aktual.core.model.Protocol
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import aktual.core.model.bytes
import aktual.test.CoTemporaryFolder
import aktual.test.TestBudgetFiles
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.emptyMockEngine
import aktual.test.enqueueResponse
import aktual.test.existsOn
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
import io.mockk.every
import io.mockk.mockk
import java.net.NoRouteToHostException
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path

class BudgetFileDownloaderTest {
  @InterceptTest val temporaryFolder = CoTemporaryFolder()

  private lateinit var budgetFileDownloader: BudgetFileDownloader
  private lateinit var budgetFiles: TestBudgetFiles
  private lateinit var apisStateHolder: AktualApisStateHolder
  private lateinit var mockEngine: MockEngine.Queue
  private lateinit var fileSystem: FileSystem

  fun TestScope.before() {
    fileSystem = FileSystem.SYSTEM
    budgetFiles = TestBudgetFiles(temporaryFolder)
    mockEngine = emptyMockEngine()

    val syncDownloadApi =
      SyncDownloadApi(
        serverUrl = SERVER_URL,
        client = testHttpClient(mockEngine, AktualJson),
        fileSystem = fileSystem,
      )

    apisStateHolder = AktualApisStateHolder()
    apisStateHolder.update {
      mockk<AktualApis>(relaxed = true) { every { syncDownload } returns syncDownloadApi }
    }

    budgetFileDownloader =
      BudgetFileDownloader(
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
      assertThatNextEmissionIsEqualTo(Failure.NotLoggedIn)
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
        assertThat(state).isInstanceOf<InProgress>()
        state = awaitItem()
      }

      // and the final state is done
      val finalState = state
      assertThat(finalState).isInstanceOf<Done>()
      assertThat(finalState.total).isEqualTo(dataSize)

      // and it contains all our data, nothing more or less
      val path = budgetFiles.encryptedZip(BUDGET_ID)
      assertThat(path).existsOn(fileSystem)
      val downloadedData = fileSystem.read(path) { readByteArray() }
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
    budgetFileDownloader.download(TOKEN, BUDGET_ID).test {
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
    budgetFileDownloader.download(TOKEN, BUDGET_ID).test {
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
