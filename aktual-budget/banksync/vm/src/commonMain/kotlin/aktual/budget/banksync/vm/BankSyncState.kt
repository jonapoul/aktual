package aktual.budget.banksync.vm

import aktual.budget.model.AccountId
import aktual.budget.model.AccountSyncSource
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable sealed interface BankSyncState

data object Loading : BankSyncState

@JvmInline value class Failure(val cause: String?) : BankSyncState

@JvmInline value class Empty(val canSync: Boolean = false) : BankSyncState

data class Success(
  val providers: ImmutableList<BankSyncProvider>,
  val unlinked: ImmutableList<BankSyncAccount>,
  val canSync: Boolean = false,
  val isSyncing: Boolean = false,
) : BankSyncState

@Immutable
data class BankSyncProvider(
  val source: AccountSyncSource,
  val status: BankSyncProviderStatus,
  val accounts: ImmutableList<BankSyncAccount>,
)

enum class BankSyncProviderStatus {
  Checking,
  Configured,
  NotConfigured,
  Failed,
  NoServer,
}

@Immutable
data class BankSyncAccount(
  val id: AccountId,
  val name: String?,
  val bankName: String?,
  val lastSync: LastBankSync,
  val status: BankSyncAccountStatus?,
  val isSyncing: Boolean = false,
)

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

@Immutable
sealed interface LastBankSync {
  data object Never : LastBankSync

  data object JustNow : LastBankSync

  @JvmInline value class MinutesAgo(val minutes: Int) : LastBankSync

  @JvmInline value class HoursAgo(val hours: Int) : LastBankSync

  @JvmInline value class DaysAgo(val days: Int) : LastBankSync
}
