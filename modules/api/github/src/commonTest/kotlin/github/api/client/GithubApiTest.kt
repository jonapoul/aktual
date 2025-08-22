package github.api.client

import actual.core.model.ServerUrl
import actual.test.emptyMockEngine
import actual.test.respondJson
import actual.test.testHttpClient
import io.ktor.client.engine.mock.MockEngine
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

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
    val request = mockEngine.requestHistory.last()

    // then
    assertEquals(
      expected = "https://test.com/repos/abc/123/releases/latest",
      actual = request.url.toString(),
    )
  }

  @Test
  fun `Query parameters inserted`() = runTest {
    // given an empty dummy response
    mockEngine += { respondJson("[]") }

    // when
    githubApi.getReleases(user = "abc", repo = "123", perPage = 420, pageNumber = 69)
    val request = mockEngine.requestHistory.last()

    // then
    assertEquals(
      expected = "https://test.com/repos/abc/123/releases/latest?per_page=420&page=69",
      actual = request.url.toString(),
    )
  }

  @Test
  fun `Partial query parameters inserted`() = runTest {
    // given an empty dummy response
    mockEngine += { respondJson("[]") }

    // when
    githubApi.getReleases(user = "abc", repo = "123", perPage = null, pageNumber = 69)
    val request = mockEngine.requestHistory.last()

    // then
    assertEquals(
      expected = "https://test.com/repos/abc/123/releases/latest?page=69",
      actual = request.url.toString(),
    )
  }

  private companion object {
    val SERVER_URL = ServerUrl("https://test.com")
  }
}
