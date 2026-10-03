package aktual.budget.banksync.vm

import aktual.budget.model.AccountId
import aktual.budget.model.AccountSyncSource
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable sealed interface BankSyncState

data object Loading : BankSyncState

@JvmInline value class Failure(val cause: String?) : BankSyncState

/** No open accounts at all, linked or not */
data object Empty : BankSyncState

data class Success(
  val providers: ImmutableList<BankSyncProvider>,
  val unlinked: ImmutableList<BankSyncAccount>,
) : BankSyncState

/** One bank sync provider (GoCardless, SimpleFIN etc.) and the open accounts linked through it */
@Immutable
data class BankSyncProvider(
  val source: AccountSyncSource,
  val status: BankSyncProviderStatus,
  val accounts: ImmutableList<BankSyncAccount>,
)

/** Whether the server has credentials for a provider, from BankSyncApi.status */
enum class BankSyncProviderStatus {
  Checking,
  Configured,
  NotConfigured,
  Failed,

  /** The budget is local-only, so there's no server to ask */
  NoServer,
}

@Immutable
data class BankSyncAccount(
  val id: AccountId,
  val name: String?,
  val bankName: String?,
  val lastSync: LastBankSync,
  val status: BankSyncAccountStatus?,
)

/** accounts.bank_sync_status, see packages/loot-core/src/types/models/account.ts */
enum class BankSyncAccountStatus {
  Ok,
  Pending,
  SyncRequested,
  Failed,
  ReauthRequired,
  AttentionRequired,
  RateLimited,
  TimedOut,
  AccountMissing,
  Unknown,
}

/** How long ago accounts.last_sync was, relative to when the screen loaded */
@Immutable
sealed interface LastBankSync {
  data object Never : LastBankSync

  data object JustNow : LastBankSync

  @JvmInline value class MinutesAgo(val minutes: Int) : LastBankSync

  @JvmInline value class HoursAgo(val hours: Int) : LastBankSync

  @JvmInline value class DaysAgo(val days: Int) : LastBankSync
}
