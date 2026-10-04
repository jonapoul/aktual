package aktual.budget.banksync.domain

import aktual.budget.model.AccountId
import aktual.budget.model.BankSyncStatus
import aktual.budget.model.TransactionId

data class BankSyncProgress(
  val isRunning: Boolean = false,
  val pending: List<AccountId> = emptyList(),
  val results: List<BankSyncResult> = emptyList(),
)

sealed interface BankSyncResult {
  val account: AccountId
  val name: String?

  data class Synced(
    override val account: AccountId,
    override val name: String?,
    val added: List<TransactionId>,
    val updated: List<TransactionId>,
  ) : BankSyncResult

  data class Failed(
    override val account: AccountId,
    override val name: String?,
    val error: BankSyncError,
  ) : BankSyncResult
}

/**
 * Why an account failed to sync, for the UI to explain as handleSyncError() in
 * packages/loot-core/src/server/accounts/app.ts does.
 */
sealed interface BankSyncError {
  /** The provider failed, e.g. the bank needs logging into again. */
  data class Provider(val category: String, val code: String, val reason: String? = null) :
    BankSyncError

  /**
   * The server refused the request, e.g. the provider isn't configured. Upstream shows [reason], or
   * says the account isn't linked properly without one.
   */
  data class Rejected(val reason: String?) : BankSyncError

  /** Anything else, like a download that couldn't be imported. */
  data class Internal(val message: String?) : BankSyncError
}

// getBankSyncStatusFromError() in packages/loot-core/src/server/accounts/app.ts
val BankSyncError.status: BankSyncStatus
  get() =
    when {
      this !is Provider -> Failed
      category == "ITEM_ERROR" && code == "ITEM_LOGIN_REQUIRED" ||
        category == "INVALID_INPUT" && code == "INVALID_ACCESS_TOKEN" ||
        category == "INVALID_ACCESS_TOKEN" -> ReauthRequired
      category == "ACCOUNT_NEEDS_ATTENTION" -> AttentionRequired
      category == "RATE_LIMIT_EXCEEDED" -> RateLimitExceeded
      category == "TIMED_OUT" -> TimedOut
      category == "ACCOUNT_MISSING" -> AccountMissing
      else -> Failed
    }
