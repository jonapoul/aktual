/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package aktual.api.client

import aktual.test.CoTemporaryFolder
import aktual.test.emptyMockEngine
import aktual.test.enqueueResponse
import aktual.test.latestRequestHeaders
import aktual.test.testHttpClient
import app.cash.burst.InterceptTest
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import io.ktor.client.engine.mock.MockEngine
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs

class SyncDownloadApiTest {
  @InterceptTest val temporaryFolder = CoTemporaryFolder()

  private lateinit var mockEngine: MockEngine.Queue
  private lateinit var syncDownloadApi: SyncDownloadApi
  private lateinit var fileSystem: FileSystem
  private lateinit var destinationPath: Path

  @BeforeTest
  fun before() {
    mockEngine = emptyMockEngine()
    fileSystem = FileSystem.SYSTEM
    destinationPath = temporaryFolder.resolve("my-file.txt")
    syncDownloadApi = SyncDownloadApi(
      serverUrl = SERVER_URL,
      client = testHttpClient(mockEngine, AktualJson),
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

    assertThat(mockEngine.latestRequestHeaders()).isEqualTo(
      mapOf(
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
          assertThat(state.contentLength).isEqualTo(length)
          assert(state.bytesSentTotal > 0)
          assert(state.bytesSentTotal <= state.contentLength)
          state = awaitItem()
        }

        assertThat(state)
          .isInstanceOf<SyncDownloadState.Done>()
          .transform { s -> fileSystem.read(s.path) { readUtf8() } }
          .isEqualTo(content)

        awaitComplete()
      }
  }

  @Test
  fun `Download bigger file`() = runTest {
    val length = 50_000_000L // 50MB
    val content = "a".repeat(length.toInt())
    mockEngine.enqueueResponse(content)

    syncDownloadApi
      .downloadUserFile(TOKEN, BUDGET_ID, destinationPath)
      .test {
        var state: SyncDownloadState = awaitItem()
        while (state is SyncDownloadState.InProgress) {
          assertThat(state.contentLength).isEqualTo(length)
          assert(state.bytesSentTotal > 0)
          assert(state.bytesSentTotal <= state.contentLength)
          state = awaitItem()
        }

        assertIs<SyncDownloadState.Done>(state)
        var savedFileSize = 0L
        fileSystem.read(state.path) {
          savedFileSize += readUtf8Line()?.length ?: error("Null line?")
        }
        assertThat(savedFileSize).isEqualTo(length)
        awaitComplete()
      }
  }
}
