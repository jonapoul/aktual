package github.api.client

import aktual.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import github.api.GithubApi
import github.api.GithubClient
import github.api.model.GithubComparison
import github.api.model.GithubRelease
import github.api.model.GithubWorkflowRuns
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.path

@ContributesBinding(AppScope::class)
class GithubApiImpl(@param:GithubClient private val client: HttpClient) : GithubApi {
  override suspend fun getReleases(
    user: String,
    repo: String,
    perPage: Int?,
    pageNumber: Int?,
  ): List<GithubRelease> =
    client
      .get {
        githubUrl("/repos/$user/$repo/releases")
        parameter(PER_PAGE, perPage)
        parameter("page", pageNumber)
      }
      .body<List<GithubRelease>>()

  override suspend fun getWorkflowRuns(
    user: String,
    repo: String,
    workflow: String,
    branch: String?,
    status: String?,
    perPage: Int?,
  ): GithubWorkflowRuns =
    client
      .get {
        githubUrl("/repos/$user/$repo/actions/workflows/$workflow/runs")
        parameter("branch", branch)
        parameter("status", status)
        parameter(PER_PAGE, perPage)
      }
      .body<GithubWorkflowRuns>()

  override suspend fun compare(
    user: String,
    repo: String,
    base: String,
    head: String,
  ): GithubComparison =
    client
      .get {
        githubUrl("/repos/$user/$repo/compare/$base...$head")
        // Only the summary is needed, not the commit list
        parameter(PER_PAGE, 1)
      }
      .body<GithubComparison>()

  private fun HttpRequestBuilder.githubUrl(urlPath: String) = url {
    protocol = HTTPS
    host = GITHUB_URL
    path(urlPath)
  }

  private companion object {
    const val GITHUB_URL = "api.github.com"
    const val PER_PAGE = "per_page"
  }
}
