package actual.about.info.ui

import github.api.client.GithubApi
import github.api.model.GithubRelease

internal class TestGithubApi(var releases: List<GithubRelease>? = null) : GithubApi {
  override suspend fun getReleases() = requireNotNull(releases)
  override fun close() = Unit
}
