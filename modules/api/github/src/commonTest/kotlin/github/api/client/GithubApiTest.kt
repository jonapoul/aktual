package github.api.client

import actual.test.emptyMockEngine
import actual.test.enqueue
import actual.test.respondJson
import actual.test.testHttpClient
import actual.url.model.ServerUrl
import io.ktor.client.engine.mock.MockEngine
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class GithubApiTest {
  private lateinit var githubApi: GithubApi
  private lateinit var mockEngine: MockEngine

  @Before
  fun before() {
    mockEngine = emptyMockEngine()
    githubApi = GithubApi(SERVER_URL, testHttpClient(mockEngine, GithubJson))
  }

  @After
  fun after() {
    mockEngine.close()
  }

  @Test
  fun `Path parameters inserted`() = runTest {
    // given an empty dummy response
    mockEngine.enqueue { respondJson("[]") }

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
    mockEngine.enqueue { respondJson("[]") }

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
    mockEngine.enqueue { respondJson("[]") }

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
