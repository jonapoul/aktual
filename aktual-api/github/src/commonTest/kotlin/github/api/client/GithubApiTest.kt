package github.api.client

import aktual.core.model.ServerUrl
import aktual.test.emptyMockEngine
import aktual.test.respondJson
import aktual.test.testHttpClient
import assertk.assertThat
import assertk.assertions.isEqualTo
import io.ktor.client.engine.mock.MockEngine
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class GithubApiTest {
  private lateinit var githubApi: GithubApi
  private lateinit var mockEngine: MockEngine.Queue

  @BeforeTest
  fun before() {
    mockEngine = emptyMockEngine()
    githubApi = GithubApi(SERVER_URL, testHttpClient(mockEngine, GithubJson))
  }

  @AfterTest
  fun after() {
    mockEngine.close()
  }

  @Test
  fun `Path parameters inserted`() = runTest {
    // given an empty dummy response
    mockEngine += { respondJson("[]") }

    // when
    githubApi.getReleases(user = "abc", repo = "123")

    // then
    assertThatRequestUrlEquals("https://test.com/repos/abc/123/releases/latest")
  }

  @Test
  fun `Query parameters inserted`() = runTest {
    // given an empty dummy response
    mockEngine += { respondJson("[]") }

    // when
    githubApi.getReleases(user = "abc", repo = "123", perPage = 420, pageNumber = 69)

    // then
    assertThatRequestUrlEquals("https://test.com/repos/abc/123/releases/latest?per_page=420&page=69")
  }

  @Test
  fun `Partial query parameters inserted`() = runTest {
    // given an empty dummy response
    mockEngine += { respondJson("[]") }

    // when
    githubApi.getReleases(user = "abc", repo = "123", perPage = null, pageNumber = 69)

    // then
    assertThatRequestUrlEquals("https://test.com/repos/abc/123/releases/latest?page=69")
  }

  private fun assertThatRequestUrlEquals(expected: String) =
    assertThat(
      mockEngine.requestHistory
        .last()
        .url
        .toString(),
    ).isEqualTo(expected)

  private companion object {
    val SERVER_URL = ServerUrl("https://test.com")
  }
}
