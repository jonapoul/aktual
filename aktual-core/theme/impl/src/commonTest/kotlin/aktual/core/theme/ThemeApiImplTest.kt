package aktual.core.theme

import aktual.api.client.AktualJson
import aktual.test.ThemeResponses
import aktual.test.emptyMockEngine
import aktual.test.latestRequest
import aktual.test.testHttpClient
import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class ThemeApiImplTest {
  private lateinit var themeApi: ThemeApi
  private lateinit var mockEngine: MockEngine.Queue

  @BeforeTest
  fun before() {
    mockEngine = emptyMockEngine()
    themeApi = ThemeApiImpl(testHttpClient(mockEngine, AktualJson))
  }

  @AfterTest
  fun after() {
    mockEngine.close()
  }

  @Test
  fun `Fetch CSS and parse as theme`() = runTest {
    // given
    mockEngine += { respond(ThemeResponses.ACTUAL_200) }

    // when
    val repo = ThemeRepo(userName = "Juulz", repoName = "shades-of-coffee")
    val response = themeApi.fetchTheme(repo)

    // then
    assertThat(response).isDataClassEqualTo(ShadesOfCoffeeTheme)
    with(mockEngine.latestRequest()) {
      assertThat(method).isEqualTo(HttpMethod.Get)
      assertThat(url)
        .isEqualTo(
          Url("https://raw.githubusercontent.com/Juulz/shades-of-coffee/refs/heads/main/actual.css")
        )
    }
  }
}
