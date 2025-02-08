package actual.about.ui

import github.api.client.GithubApi
import github.api.model.GithubReleases

internal class TestGithubApi(var releases: GithubReleases? = null) : GithubApi {
  override suspend fun getReleases(): GithubReleases = requireNotNull(releases)
}
