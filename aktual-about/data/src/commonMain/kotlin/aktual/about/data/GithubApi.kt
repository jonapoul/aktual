package aktual.about.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.URLProtocol
import io.ktor.http.path
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Instant

interface GithubApi {
  suspend fun getReleases(
    user: String,
    repo: String,
    perPage: Int? = null,
    pageNumber: Int? = null,
  ): List<GithubRelease>
}

class GithubApiImpl(private val client: HttpClient) : GithubApi {
  override suspend fun getReleases(
    user: String,
    repo: String,
    perPage: Int?,
    pageNumber: Int?
  ): List<GithubRelease> = client.get {
    url {
      protocol = URLProtocol.HTTPS
      host = GITHUB_URL
      path("/repos/$user/$repo/releases/latest")
    }
    parameter("per_page", perPage)
    parameter("page", pageNumber)
  }.body<List<GithubRelease>>()
}

private const val GITHUB_URL = "api.github.com"

val GithubJson = Json {
  ignoreUnknownKeys = true
}

/**
 * A subset of the fields returned by https://docs.github.com/en/rest/releases/releases?apiVersion=2022-11-28#list-releases
 *
 * [versionName] - The title (version string) of the release. E.g. "1.2.3"
 * [publishedAt] - The ISO-8601 string timestamp of the publish. E.g. "2021-11-06T12:15:10Z"
 * [htmlUrl] - The URL of the release tag. E.g. "https://github.com/jonapoul/aktual/releases/tag/1.0.0"
 * [tagName] - The name of the tag, e.g. "v1.2.3"
 */
@Serializable
data class GithubRelease(
  @SerialName("name") val versionName: String,
  @SerialName("published_at") val publishedAt: Instant,
  @SerialName("html_url") val htmlUrl: String,
  @SerialName("tag_name") val tagName: String,
)
