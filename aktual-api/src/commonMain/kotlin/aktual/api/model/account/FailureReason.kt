package aktual.api.model.account

import kotlinx.serialization.Serializable

/**
 * See validateSession() in
 * https://github.com/actualbudget/actual/blob/master/packages/sync-server/src/util/validate-user.ts
 * and the /sync handler in packages/sync-server/src/app-sync.ts
 */
@JvmInline
@Serializable
value class FailureReason(val reason: String) {
  companion object {
    val AlreadyBootstrapped = FailureReason("already-bootstrapped")
    val ApiKeyNotFound = FailureReason("api-key-not-found")
    val ClockDrift = FailureReason("clock-drift")
    val FileAccessDenied = FailureReason("file-access-denied")
    val FileNotFound = FailureReason("file-not-found")
    val InvalidApiKey = FailureReason("invalid-api-key")
    val InvalidPassword = FailureReason("invalid-password")
    val NotAdmin = FailureReason("not-admin")
    val TokenExpired = FailureReason("token-expired")
    val Unauthorized = FailureReason("unauthorized")
    val UnprocessableEntity = FailureReason("unprocessable-entity")
    val UserNotFound = FailureReason("user-not-found")
  }
}
