package github.api

import github.api.model.GithubRelease

interface GithubApi {
  suspend fun getReleases(
    user: String,
    repo: String,
    perPage: Int? = null,
    pageNumber: Int? = null,
  ): List<GithubRelease>
}
