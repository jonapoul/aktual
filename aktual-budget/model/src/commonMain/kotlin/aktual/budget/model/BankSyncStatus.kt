@file:Suppress("FunctionName")

package aktual.budget.model

// packages/loot-core/src/types/models/account.ts BankSyncStatus
@JvmInline
value class BankSyncStatus private constructor(val value: String) {
  // The last sync didn't complete, so the account needs looking at
  val isFailure: Boolean
    get() =
      when (this) {
        Failed,
        ReauthRequired,
        AttentionRequired,
        RateLimitExceeded,
        TimedOut,
        AccountMissing -> true
        else -> false
      }

  override fun toString(): String = value

  companion object {
    val Ok = BankSyncStatus(value = "ok")
    val Pending = BankSyncStatus(value = "pending")
    val SyncRequested = BankSyncStatus(value = "sync-requested")
    val Failed = BankSyncStatus(value = "failed")
    val ReauthRequired = BankSyncStatus(value = "reauth-required")
    val AttentionRequired = BankSyncStatus(value = "attention-required")
    val RateLimitExceeded = BankSyncStatus(value = "rate-limit-exceeded")
    val TimedOut = BankSyncStatus(value = "timed-out")
    val AccountMissing = BankSyncStatus(value = "account-missing")

    fun Other(value: String) = BankSyncStatus(value)

    fun fromString(string: String): BankSyncStatus =
      when (string) {
        Ok.value -> Ok
        Pending.value -> Pending
        SyncRequested.value -> SyncRequested
        Failed.value -> Failed
        ReauthRequired.value -> ReauthRequired
        AttentionRequired.value -> AttentionRequired
        RateLimitExceeded.value -> RateLimitExceeded
        TimedOut.value -> TimedOut
        AccountMissing.value -> AccountMissing
        else -> Other(string)
      }
  }
}
