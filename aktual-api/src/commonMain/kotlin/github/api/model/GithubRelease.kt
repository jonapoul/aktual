package github.api.model

import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubRelease(
  @SerialName("name") val versionName: String,
  @SerialName("published_at") val publishedAt: Instant?, // Null for drafts
  @SerialName("html_url") val htmlUrl: String,
  @SerialName("tag_name") val tagName: String,
  @SerialName("prerelease") val isPrerelease: Boolean = false,
  @SerialName("draft") val isDraft: Boolean = false,
)
