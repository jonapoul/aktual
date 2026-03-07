package aktual.core.theme

import aktual.test.ThemeResponses
import aktual.test.emptyMockEngine
import aktual.test.latestRequest
import aktual.test.respondJson
import aktual.test.testHttpClient
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
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
    themeApi = ThemeApiImpl(testHttpClient(mockEngine, ThemeJson))
  }

  @AfterTest
  fun after() {
    mockEngine.close()
  }

  @Test
  fun `Fetch CSS and parse as theme`() = runTest {
    // given
    mockEngine += { respond(ThemeResponses.CUSTOM_THEME_200) }

    // when
    val response = themeApi.fetchTheme(ShadesOfCoffeeThemeSummary)

    // then
    assertThat(response).isDataClassEqualTo(ShadesOfCoffeeTheme)
    with(mockEngine.latestRequest()) {
      assertThat(method).isEqualTo(HttpMethod.Get)
      assertThat(url.toString())
        .isEqualTo(
          "https://raw.githubusercontent.com/Juulz/shades-of-coffee/refs/heads/main/actual.css"
        )
    }
  }

  @Test
  fun `Fetch catalog`() = runTest {
    // given
    mockEngine += { respondJson(ThemeResponses.CUSTOM_THEME_CATALOG_200) }

    // when
    val response = themeApi.fetchCatalog()

    // then
    assertThat(response).contains(ShadesOfCoffeeThemeSummary)
    with(mockEngine.latestRequest()) {
      assertThat(method).isEqualTo(HttpMethod.Get)
      assertThat(url.toString())
        .isEqualTo(
          "https://raw.githubusercontent.com/actualbudget/actual/master/packages/desktop-client/" +
            "src/data/customThemeCatalog.json"
        )
    }
  }
}
