package github.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Compares base to head, so status is "ahead" when head has commits that base doesn't
@Serializable
data class GithubComparison(
  @SerialName("status") val status: String,
  @SerialName("ahead_by") val aheadBy: Int,
)
