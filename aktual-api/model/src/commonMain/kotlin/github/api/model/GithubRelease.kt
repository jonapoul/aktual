package github.api.model

import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubRelease(
  // The title (version string) of the release, e.g. "1.2.3"
  @SerialName("name") val versionName: String,

  // The ISO-8601 string timestamp of the publish. E.g. "2021-11-06T12:15:10Z"
  @SerialName("published_at") val publishedAt: Instant,

  // The URL of the release tag. E.g. "https://github.com/jonapoul/aktual/releases/tag/1.0.0"
  @SerialName("html_url") val htmlUrl: String,

  // The name of the tag, e.g. "v1.2.3"
  @SerialName("tag_name") val tagName: String,
)
