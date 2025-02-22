package actual.api.model.account

import actual.api.model.internal.FailureReasonSerializer
import alakazam.kotlin.core.requireMessage
import kotlinx.serialization.Serializable

/**
 * See validateSession() in actual-server/src/util/validate-user.js
 */
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

fun String.toFailureReason() = FailureReason.Other(this)

fun Exception.toFailureReason() = FailureReason.Other(requireMessage())
