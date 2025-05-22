package actual.api.model.account

import actual.api.model.internal.FailureReasonSerializer
import dev.drewhamilton.poko.Poko
import kotlinx.serialization.Serializable

/**
 * See validateSession() in actual-server/src/util/validate-user.js
 */
@Serializable(FailureReasonSerializer::class)
sealed interface FailureReason {
  val reason: String

  enum class Known(override val reason: String) : FailureReason {
    TokenExpired("token-expired"),
    Unauthorized("unauthorized"),
    FileNotFound("file-not-found"),
  }

  @Poko class Other(
    override val reason: String,
  ) : FailureReason
}
