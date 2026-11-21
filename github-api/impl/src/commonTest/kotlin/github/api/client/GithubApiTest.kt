package github.api.client

import aktual.test.emptyMockEngine
import aktual.test.respondJson
import aktual.test.testHttpClient
import assertk.assertThat
import assertk.assertions.isEqualTo
import github.api.GithubApi
import github.api.model.GithubComparison
import io.ktor.client.engine.mock.MockEngine
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class GithubApiTest {
  private lateinit var githubApi: GithubApi
  private lateinit var mockEngine: MockEngine.Queue

  @BeforeTest
  fun before() {
    mockEngine = emptyMockEngine()
    githubApi = GithubApiImpl(testHttpClient(mockEngine, GithubJson))
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
    assertThatRequestUrlEquals("https://api.github.com/repos/abc/123/releases")
  }

  @Test
  fun `Query parameters inserted`() = runTest {
    // given an empty dummy response
    mockEngine += { respondJson("[]") }

    // when
    githubApi.getReleases(user = "abc", repo = "123", perPage = 420, pageNumber = 69)

    // then
    assertThatRequestUrlEquals(
      "https://api.github.com/repos/abc/123/releases?per_page=420&page=69",
    )
  }

  @Test
  fun `Partial query parameters inserted`() = runTest {
    // given an empty dummy response
    mockEngine += { respondJson("[]") }

    // when
    githubApi.getReleases(user = "abc", repo = "123", perPage = null, pageNumber = 69)

    // then
    assertThatRequestUrlEquals("https://api.github.com/repos/abc/123/releases?page=69")
  }

  @Test
  fun `Workflow runs filtered by branch and status`() = runTest {
    mockEngine += { respondJson("""{ "workflow_runs": [] }""") }

    githubApi.getWorkflowRuns(
      user = "abc",
      repo = "123",
      workflow = "nightly.yml",
      branch = "main",
      status = "success",
      perPage = 1,
    )

    assertThatRequestUrlEquals(
      "https://api.github.com/repos/abc/123/actions/workflows/nightly.yml/runs" +
        "?branch=main&status=success&per_page=1",
    )
  }

  @Test
  fun `Compare puts both commits in the path`() = runTest {
    mockEngine += { respondJson("""{ "status": "ahead", "ahead_by": 2 }""") }

    val comparison = githubApi.compare(user = "abc", repo = "123", base = "aaa", head = "bbb")

    assertThatRequestUrlEquals("https://api.github.com/repos/abc/123/compare/aaa...bbb?per_page=1")
    assertThat(comparison).isEqualTo(GithubComparison(status = "ahead", aheadBy = 2))
  }

  private fun assertThatRequestUrlEquals(expected: String) =
    assertThat(mockEngine.requestHistory).transform { it.last().url.toString() }.isEqualTo(expected)
}
