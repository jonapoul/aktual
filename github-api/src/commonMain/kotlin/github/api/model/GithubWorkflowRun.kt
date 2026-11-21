package github.api.model

import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubWorkflowRuns(@SerialName("workflow_runs") val runs: List<GithubWorkflowRun>)

@Serializable
data class GithubWorkflowRun(
  @SerialName("id") val id: Long,
  @SerialName("head_sha") val headSha: String,
  @SerialName("head_commit") val headCommit: GithubHeadCommit,
)

@Serializable data class GithubHeadCommit(@SerialName("timestamp") val timestamp: Instant)
