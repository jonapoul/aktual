package actual.about.info.ui

import github.api.client.GithubApi
import github.api.model.GithubRelease
import kotlin.test.fail

internal class TestGithubApi(var releases: List<GithubRelease>? = null) : GithubApi {
  override suspend fun getReleases(user: String, repo: String, perPage: Int?, pageNumber: Int?) =
    releases ?: fail("No releases set!")

  override fun close() = Unit
}
