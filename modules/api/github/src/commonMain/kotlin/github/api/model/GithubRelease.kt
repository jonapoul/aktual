package github.api.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A subset of the fields returned by https://docs.github.com/en/rest/releases/releases?apiVersion=2022-11-28#list-releases
 */
@Serializable
data class GithubRelease(
  // The title (version string) of the release. E.g. "1.2.3"
  @SerialName("name") val versionName: String,

  // The ISO-8601 string timestamp of the publish. E.g. "2021-11-06T12:15:10Z"
  @SerialName("published_at") val publishedAt: Instant,

  // The URL of the release tag. E.g. "https://github.com/jonapoul/actual-android/releases/tag/1.0.0"
  @SerialName("html_url") val htmlUrl: String,

  // The name of the tag, e.g. "v1.2.3"
  @SerialName("tag_name") val tagName: String,
)
