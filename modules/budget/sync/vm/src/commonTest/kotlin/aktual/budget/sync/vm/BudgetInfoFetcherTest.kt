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
package aktual.budget.sync.vm

import aktual.api.client.AktualApis
import aktual.api.client.AktualApisStateHolder
import aktual.api.client.AktualJson
import aktual.api.client.SyncApi
import aktual.api.model.sync.EncryptMeta
import aktual.api.model.sync.UserFile
import aktual.budget.model.BudgetId
import aktual.budget.sync.vm.BudgetInfoFetcher.Result
import aktual.core.model.KeyId
import aktual.core.model.LoginToken
import aktual.core.model.ServerUrl
import aktual.core.model.base64
import aktual.test.emptyMockEngine
import aktual.test.respondJson
import aktual.test.testHttpClient
import alakazam.test.core.TestCoroutineContexts
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.matchesPredicate
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.HttpStatusCode
import io.mockk.mockk
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import java.net.NoRouteToHostException
import kotlin.test.AfterTest
import kotlin.test.Test

class BudgetInfoFetcherTest {
  private lateinit var budgetInfoFetcher: BudgetInfoFetcher
  private lateinit var apisStateHolder: AktualApisStateHolder
  private lateinit var mockEngine: MockEngine.Queue
  private lateinit var client: HttpClient
  private lateinit var syncApi: SyncApi

  @AfterTest
  fun after() {
    mockEngine.close()
  }

  private fun TestScope.before() {
    mockEngine = emptyMockEngine()
    client = testHttpClient(mockEngine, AktualJson)
    syncApi = SyncApi(SERVER_URL, client)
    apisStateHolder = AktualApisStateHolder()
    apisStateHolder.update { aktualApis() }
    budgetInfoFetcher = BudgetInfoFetcher(
      contexts = TestCoroutineContexts(StandardTestDispatcher(testScheduler)),
      apisStateHolder = apisStateHolder,
    )
  }

  @Test
  fun `Handle success`() = runTest {
    // given
    before()
    val response = """
      {
          "status": "ok",
          "data": {
              "deleted": 0,
              "fileId": "b328186c-c819-4333-959b-04f676c1ee46",
              "groupId": "afb25fc0-b294-4f71-ae8f-ce1e4a8fec10",
              "name": "Main Budget",
              "encryptMeta": {
                  "keyId": "2a66f5de-c530-4c06-8103-a48f26a0ce44",
                  "algorithm": "aes-256-gcm",
                  "iv": "7tzgaLCrSFxVfzZR",
                  "authTag": "25nafe0UpzehRCks/xQjoB=="
              },
              "usersWithAccess": []
          }
      }
    """.trimIndent()
    mockEngine += { respondJson(response) }

    // then
    val userFile = UserFile(
      deleted = 0,
      fileId = BudgetId("b328186c-c819-4333-959b-04f676c1ee46"),
      groupId = "afb25fc0-b294-4f71-ae8f-ce1e4a8fec10",
      name = "Main Budget",
      encryptKeyId = null,
      owner = null,
      usersWithAccess = emptyList(),
      encryptMeta = EncryptMeta(
        keyId = KeyId("2a66f5de-c530-4c06-8103-a48f26a0ce44"),
        algorithm = "aes-256-gcm",
        iv = "7tzgaLCrSFxVfzZR".base64,
        authTag = "25nafe0UpzehRCks/xQjoB==".base64,
      ),
    )
    assertThatFetchResult().isEqualTo(Result.Success(userFile))
  }

  @Test
  fun `Not logged in if no API client cached`() = runTest {
    // given
    before()
    apisStateHolder.reset()

    // then
    assertThatFetchResult().isEqualTo(Result.NotLoggedIn)
  }

  @Test
  fun `Handle HTTP failure`() = runTest {
    // given
    before()
    mockEngine += {
      respondJson(
        status = HttpStatusCode.Unauthorized,
        content = """{ "status": "error", "reason": "unauthorized", "details": "token-not-found" }""",
      )
    }

    // then
    assertThatFetchResult().isEqualTo(Result.HttpFailure("unauthorized"))
  }

  @Test
  fun `Handle unexpected HTTP failure body`() = runTest {
    // given
    before()
    mockEngine += {
      respondJson(
        status = HttpStatusCode.Unauthorized,
        content = """{ "unexpected-key": 123 }""",
      )
    }

    // then
    assertThatFetchResult()
      .isInstanceOf<Result.HttpFailure>()
      .matchesPredicate { it.reason.contains("failed parsing") }
  }

  @Test
  fun `Handle network error`() = runTest {
    // given
    before()
    mockEngine += { throw NoRouteToHostException() }

    // then
    assertThatFetchResult()
      .isInstanceOf<Result.IOFailure>()
  }

  private suspend fun assertThatFetchResult() = assertThat(budgetInfoFetcher.fetch(TOKEN, BUDGET_ID))

  private fun aktualApis() = AktualApis(
    serverUrl = SERVER_URL,
    client = client,
    account = mockk(),
    base = mockk(),
    health = mockk(),
    metrics = mockk(),
    sync = syncApi,
    syncDownload = mockk(),
  )

  private companion object {
    val TOKEN = LoginToken("abc-123")
    val BUDGET_ID = BudgetId("xyz-789")
    val SERVER_URL = ServerUrl("https://website.com")
  }
}
