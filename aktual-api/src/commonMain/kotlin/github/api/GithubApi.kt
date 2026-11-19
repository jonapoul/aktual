package github.api

import github.api.model.GithubComparison
import github.api.model.GithubRelease
import github.api.model.GithubWorkflowRuns

interface GithubApi {
  suspend fun getReleases(
    user: String,
    repo: String,
    perPage: Int? = null,
    pageNumber: Int? = null,
  ): List<GithubRelease>

  suspend fun getWorkflowRuns(
    user: String,
    repo: String,
    workflow: String,
    branch: String? = null,
    status: String? = null,
    perPage: Int? = null,
  ): GithubWorkflowRuns

  suspend fun compare(user: String, repo: String, base: String, head: String): GithubComparison
}
