package github.api.client

import github.api.model.GithubRelease
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.URLProtocol
import io.ktor.http.path

/**
 * From https://docs.github.com/en/rest/releases/releases?apiVersion=2022-11-28#list-releases
 */
interface GithubApi : AutoCloseable {
  suspend fun getReleases(): List<GithubRelease>

  fun interface Factory {
    fun build(): GithubApi
  }
}

fun GithubApi(client: HttpClient): GithubApi = GithubApiClient(client)

private class GithubApiClient(private val client: HttpClient) : GithubApi, AutoCloseable by client {
  override suspend fun getReleases() = client
    .get {
      url {
        protocol = URLProtocol.HTTPS
        host = BASE_URL
        path("/repos/jonapoul/actual-android/releases/latest")
      }
    }.body<List<GithubRelease>>()

  private companion object {
    const val BASE_URL = "api.github.com"
  }
}
