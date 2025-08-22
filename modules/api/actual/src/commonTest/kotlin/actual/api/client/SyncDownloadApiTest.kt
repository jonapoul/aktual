package actual.api.client

import actual.test.emptyMockEngine
import actual.test.enqueueResponse
import actual.test.latestRequestHeaders
import actual.test.testHttpClient
import app.cash.turbine.test
import io.ktor.client.engine.mock.MockEngine
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class SyncDownloadApiTest {
  @get:Rule val temporaryFolder = TemporaryFolder()

  private lateinit var mockEngine: MockEngine.Queue
  private lateinit var syncDownloadApi: SyncDownloadApi
  private lateinit var fileSystem: FileSystem
  private lateinit var destinationPath: Path

  @BeforeTest
  fun before() {
    mockEngine = emptyMockEngine()
    fileSystem = FileSystem.SYSTEM
    destinationPath = temporaryFolder.root.resolve("my-file.txt").toOkioPath()
    syncDownloadApi = SyncDownloadApi(
      serverUrl = SERVER_URL,
      client = testHttpClient(mockEngine, ActualJson),
      fileSystem = fileSystem,
    )
  }

  @AfterTest
  fun after() {
    mockEngine.close()
  }

  @Test
  fun `Download user files request headers`() = runTest {
    mockEngine.enqueueResponse(content = "abc123")

    syncDownloadApi.downloadUserFile(TOKEN, BUDGET_ID, destinationPath).collect {
      // don't care for this test
    }

    assertEquals(
      actual = mockEngine.latestRequestHeaders(),
      expected = mapOf(
        "X-ACTUAL-TOKEN" to listOf("abc-123"),
        "X-ACTUAL-FILE-ID" to listOf("xyz-789"),
        "Accept" to listOf("application/json"),
        "Accept-Charset" to listOf("UTF-8"),
      ),
    )
  }

  @Test
  fun `Download small file`() = runTest {
    val length = 1_000L // 1kB
    val content = "a".repeat(length.toInt())
    mockEngine.enqueueResponse(content)

    syncDownloadApi
      .downloadUserFile(TOKEN, BUDGET_ID, destinationPath)
      .test {
        var state: SyncDownloadState = awaitItem()
        while (state is SyncDownloadState.InProgress) {
          assertNotNull(state.contentLength)
          assertEquals(expected = length, actual = state.contentLength)
          assert(state.bytesSentTotal > 0)
          assert(state.bytesSentTotal <= state.contentLength)
          state = awaitItem()
        }

        assertIs<SyncDownloadState.Done>(state)
        val savedContent = fileSystem.read(state.path) { readUtf8() }
        assertEquals(expected = content, actual = savedContent)
        awaitComplete()
      }
  }

  @Test
  fun `Download bigger file`() = runTest {
    val length = 50_000_000L // 50kB
    val content = "a".repeat(length.toInt())
    mockEngine.enqueueResponse(content)

    syncDownloadApi
      .downloadUserFile(TOKEN, BUDGET_ID, destinationPath)
      .test {
        var state: SyncDownloadState = awaitItem()
        while (state is SyncDownloadState.InProgress) {
          assertNotNull(state.contentLength)
          assertEquals(expected = length, actual = state.contentLength)
          assert(state.bytesSentTotal > 0)
          assert(state.bytesSentTotal <= state.contentLength)
          state = awaitItem()
        }

        assertIs<SyncDownloadState.Done>(state)
        var savedFileSize = 0L
        fileSystem.read(state.path) {
          savedFileSize += readUtf8Line()?.length ?: error("Null line?")
        }
        assertEquals(expected = length, actual = savedFileSize)
        awaitComplete()
      }
  }
}
