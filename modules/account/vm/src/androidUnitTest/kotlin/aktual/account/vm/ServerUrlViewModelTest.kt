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
@file:OptIn(ExperimentalCoroutinesApi::class)

package aktual.account.vm

import aktual.api.client.AccountApi
import aktual.api.client.AktualApis
import aktual.api.client.AktualApisStateHolder
import aktual.api.client.AktualJson
import aktual.core.model.AktualVersionsStateHolder
import aktual.core.model.LoginToken
import aktual.core.model.Protocol
import aktual.core.model.ServerUrl
import aktual.prefs.AppGlobalPreferences
import aktual.test.TestBuildConfig
import aktual.test.assertThatNextEmission
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.buildPreferences
import aktual.test.clear
import aktual.test.emptyMockEngine
import aktual.test.respondJson
import aktual.test.testHttpClient
import alakazam.test.core.TestCoroutineContexts
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.HttpStatusCode
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
class ServerUrlViewModelTest {
  // Real
  private lateinit var preferences: AppGlobalPreferences
  private lateinit var viewModel: ServerUrlViewModel
  private lateinit var apisStateHolder: AktualApisStateHolder
  private lateinit var versionsStateHolder: AktualVersionsStateHolder

  // Mock
  private lateinit var apis: AktualApis
  private lateinit var accountApi: AccountApi
  private lateinit var mockEngine: MockEngine.Queue

  @BeforeTest
  fun before() {
    versionsStateHolder = AktualVersionsStateHolder(TestBuildConfig)
    mockEngine = emptyMockEngine()
    accountApi = AccountApi(EXAMPLE_URL, testHttpClient(mockEngine, AktualJson))
    apis = mockk {
      every { close() } just runs
      every { serverUrl } returns EXAMPLE_URL
      every { account } returns accountApi
    }
    apisStateHolder = AktualApisStateHolder()
    apisStateHolder.update { apis }
  }

  @AfterTest
  fun after() {
    mockEngine.close()
  }

  private fun TestScope.buildPreferences() {
    val prefs = buildPreferences(UnconfinedTestDispatcher(testScheduler))
    preferences = AppGlobalPreferences(prefs)
    preferences.serverUrl.set(EXAMPLE_URL)
  }

  private fun TestScope.buildViewModel() {
    viewModel = ServerUrlViewModel(
      contexts = TestCoroutineContexts(StandardTestDispatcher(testScheduler)),
      apiStateHolder = apisStateHolder,
      preferences = preferences,
      versionsStateHolder = versionsStateHolder,
      buildConfig = TestBuildConfig.copy(defaultServerUrl = null),
    )
  }

  @Test
  fun `Nav to login when typing and clicking confirm if already bootstrapped`() = runTest {
    buildPreferences()
    buildViewModel()
    viewModel.navDestination.receiveAsFlow().test {
      // Given we're currently not navigating, and the API returns that we're bootstrapped
      advanceUntilIdle()
      ensureAllEventsConsumed()
      setBootstrapResponse(bootstrapped = true)

      // When
      viewModel.onSelectProtocol(EXAMPLE_URL.protocol)
      viewModel.onEnterUrl(EXAMPLE_URL.baseUrl)
      viewModel.onClickConfirm()

      // Then
      assertThatNextEmissionIsEqualTo(NavDestination.ToLogin)
      advanceUntilIdle()
      ensureAllEventsConsumed()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Nav to bootstrap when typing and clicking confirm if not bootstrapped`() = runTest {
    buildPreferences()
    buildViewModel()
    viewModel.navDestination.receiveAsFlow().test {
      // Given we're currently not navigating, and the API returns that we're not bootstrapped
      advanceUntilIdle()
      ensureAllEventsConsumed()
      setBootstrapResponse(bootstrapped = false)

      // When
      viewModel.onSelectProtocol(EXAMPLE_URL.protocol)
      viewModel.onEnterUrl(EXAMPLE_URL.baseUrl)
      viewModel.onClickConfirm()

      // Then
      assertThatNextEmissionIsEqualTo(NavDestination.ToBootstrap)
      advanceUntilIdle()
      ensureAllEventsConsumed()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Set initial parameters based on preferences`() = runTest {
    // Given
    buildPreferences()
    preferences.serverUrl.set(EXAMPLE_URL)

    // When
    buildViewModel()
    advanceUntilIdle()

    // Then
    val protocol = viewModel.protocol.value
    val baseUrl = viewModel.baseUrl.value
    assertThat(protocol).isEqualTo(EXAMPLE_URL.protocol)
    assertThat(baseUrl).isEqualTo(EXAMPLE_URL.baseUrl)
  }

  @Test
  fun `Show error message if bootstrap request gives failure response`() = runTest {
    buildPreferences()
    buildViewModel()

    viewModel.errorMessage.test {
      // Given we're currently not navigating
      assertThatNextEmission().isNull()

      // and the API returns failure
      val reason = "SOMETHING BROKE"
      val body = """
        {
          "status": "error",
          "reason": "$reason"
        }
      """.trimIndent()
      mockEngine.clear()
      mockEngine += { respondJson(body, HttpStatusCode.BadRequest) }

      // When
      viewModel.onSelectProtocol(EXAMPLE_URL.protocol)
      viewModel.onEnterUrl(EXAMPLE_URL.baseUrl)
      viewModel.onClickConfirm()

      // Then
      assertThat(awaitItem())
        .isNotNull()
        .contains(reason)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Show error message if bootstrap request fails`() = runTest {
    buildPreferences()
    buildViewModel()
    viewModel.errorMessage.test {
      // Given we're currently not navigating, and the API returns that we're not bootstrapped
      assertThatNextEmission().isNull()

      // and the API fails
      val reason = "SOMETHING BROKE"
      mockEngine.clear()
      mockEngine += { throw IOException(reason) }

      // When
      viewModel.onSelectProtocol(EXAMPLE_URL.protocol)
      viewModel.onEnterUrl(EXAMPLE_URL.baseUrl)
      viewModel.onClickConfirm()

      // Then
      assertThat(awaitItem())
        .isNotNull()
        .contains(reason)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Clear saved token if the confirmed URL is different from previously-saved`() = runTest {
    buildPreferences()
    buildViewModel()
    preferences.loginToken.asFlow().test {
      // Given no token initially saved
      assertThatNextEmission().isNull()

      // when we save a token and a URL
      val initialUrl = ServerUrl(Protocol.Https, "website.com")
      preferences.serverUrl.setAndCommit(initialUrl)
      val token = LoginToken("abc-123")
      preferences.loginToken.setAndCommit(token)

      // then the token has been saved
      assertThatNextEmissionIsEqualTo(token)

      // when we enter a different url and click confirm
      val secondUrl = ServerUrl(Protocol.Http, "some.other.website.com")
      assertThat(initialUrl).isNotEqualTo(secondUrl)
      viewModel.onSelectProtocol(secondUrl.protocol)
      viewModel.onEnterUrl(secondUrl.baseUrl)
      viewModel.onClickConfirm()

      // then the saved token is cleared
      assertThatNextEmission().isNull()
      cancelAndIgnoreRemainingEvents()
    }
  }

  private fun setBootstrapResponse(bootstrapped: Boolean) {
    val json = """
      {
          "status": "ok",
          "data": {
              "bootstrapped": $bootstrapped,
              "loginMethod": "password",
              "availableLoginMethods": [
                  {
                      "method": "password",
                      "active": 1,
                      "displayName": "Password"
                  }
              ],
              "multiuser": false
          }
      }
    """.trimIndent()
    mockEngine.clear()
    mockEngine += { respondJson(json) }
  }

  private companion object {
    val EXAMPLE_URL = ServerUrl(Protocol.Http, "website.com")
  }
}
