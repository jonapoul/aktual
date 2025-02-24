package actual.api.model.account

import actual.api.model.internal.FailureReasonSerializer
import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * See validateSession() in actual-server/src/util/validate-user.js
 */
@Immutable
@Serializable(FailureReasonSerializer::class)
sealed interface FailureReason {
  val reason: String

  data object TokenExpired : FailureReason {
    override val reason = "token-expired"
  }

  data object Unauthorized : FailureReason {
    override val reason = "unauthorized"
  }

  data class Other(
    override val reason: String,
  ) : FailureReason
}
