package aktual.api.model.account

import kotlinx.serialization.Serializable

/**
 * See validateSession() in actual-server/src/util/validate-user.js
 */
@JvmInline
@Serializable
value class FailureReason(val reason: String) {
  companion object {
    val AlreadyBootstrapped = FailureReason("already-bootstrapped")
    val FileNotFound = FailureReason("file-not-found")
    val InvalidPassword = FailureReason("invalid-password")
    val TokenExpired = FailureReason("token-expired")
    val Unauthorized = FailureReason("unauthorized")
    val UnprocessableEntity = FailureReason("unprocessable-entity")
    val UserNotFound = FailureReason("user-not-found")
  }
}
