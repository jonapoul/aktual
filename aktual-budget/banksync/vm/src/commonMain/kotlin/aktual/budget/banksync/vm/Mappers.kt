package aktual.budget.banksync.vm

import aktual.budget.db.GetBankSyncAccounts
import aktual.budget.model.BankSyncStatus
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

internal fun GetBankSyncAccounts.toBankSyncAccount(now: Instant) =
  BankSyncAccount(
    id = id,
    name = name,
    bankName = bank_name,
    lastSync = lastBankSync(last_sync, now),
    status = bank_sync_status?.toAccountStatus(),
  )

// Upstream treats an account as linked if it has a sync source, and unlinking clears both
internal val GetBankSyncAccounts.isLinked: Boolean
  get() = account_sync_source != null && account_id != null

internal fun lastBankSync(lastSync: Instant?, now: Instant): LastBankSync {
  if (lastSync == null) return LastBankSync.Never
  val elapsed = now - lastSync
  return when {
    elapsed < 1.minutes -> LastBankSync.JustNow
    elapsed < 1.hours -> LastBankSync.MinutesAgo(elapsed.inWholeMinutes.toInt())
    elapsed < 1.days -> LastBankSync.HoursAgo(elapsed.inWholeHours.toInt())
    else -> LastBankSync.DaysAgo(elapsed.inWholeDays.toInt())
  }
}

internal fun BankSyncStatus.toAccountStatus(): BankSyncAccountStatus =
  when (this) {
    BankSyncStatus.Ok -> BankSyncAccountStatus.Ok
    BankSyncStatus.Pending -> BankSyncAccountStatus.Pending
    BankSyncStatus.SyncRequested -> BankSyncAccountStatus.SyncRequested
    BankSyncStatus.Failed -> BankSyncAccountStatus.Failed
    BankSyncStatus.ReauthRequired -> BankSyncAccountStatus.ReauthRequired
    BankSyncStatus.AttentionRequired -> BankSyncAccountStatus.AttentionRequired
    BankSyncStatus.RateLimitExceeded -> BankSyncAccountStatus.RateLimited
    BankSyncStatus.TimedOut -> BankSyncAccountStatus.TimedOut
    BankSyncStatus.AccountMissing -> BankSyncAccountStatus.AccountMissing
    else -> BankSyncAccountStatus.Unknown
  }
