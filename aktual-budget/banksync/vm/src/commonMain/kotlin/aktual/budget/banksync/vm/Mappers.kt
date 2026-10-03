package aktual.budget.banksync.vm

import aktual.budget.banksync.vm.LastBankSync.DaysAgo
import aktual.budget.banksync.vm.LastBankSync.HoursAgo
import aktual.budget.banksync.vm.LastBankSync.MinutesAgo
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
  if (lastSync == null) return Never
  val elapsed = now - lastSync
  return when {
    elapsed < 1.minutes -> JustNow
    elapsed < 1.hours -> MinutesAgo(elapsed.inWholeMinutes.toInt())
    elapsed < 1.days -> HoursAgo(elapsed.inWholeHours.toInt())
    else -> DaysAgo(elapsed.inWholeDays.toInt())
  }
}

internal fun BankSyncStatus.toAccountStatus(): BankSyncAccountStatus =
  when (this) {
    Ok -> Ok
    Pending -> Pending
    SyncRequested -> SyncRequested
    Failed -> Failed
    ReauthRequired -> ReauthRequired
    AttentionRequired -> AttentionRequired
    RateLimitExceeded -> RateLimited
    TimedOut -> TimedOut
    AccountMissing -> AccountMissing
    else -> Unknown
  }
