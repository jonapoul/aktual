package github.api.client

import aktual.api.client.GithubClient
import aktual.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import github.api.GithubApi
import github.api.model.GithubRelease
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.URLProtocol
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
        url {
          protocol = URLProtocol.HTTPS
          host = GITHUB_URL
          path("/repos/$user/$repo/releases/latest")
        }
        parameter("per_page", perPage)
        parameter("page", pageNumber)
      }
      .body<List<GithubRelease>>()

  private companion object {
    const val GITHUB_URL = "api.github.com"
  }
}
