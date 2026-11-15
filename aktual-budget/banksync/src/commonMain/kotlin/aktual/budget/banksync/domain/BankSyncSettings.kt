package aktual.budget.banksync.domain

import aktual.budget.banksync.domain.SyncMappings.Companion.Default
import aktual.budget.model.AccountId

/**
 * An account's bank sync preferences, as packages/loot-core/src/server/accounts/sync.ts reads them.
 */
data class BankSyncSettings(
  val importNotes: Boolean = true,
  val importPending: Boolean = true,
  val importTransactions: Boolean = true,
  val reimportDeleted: Boolean = true,
  val updateDates: Boolean = false,
  val mappings: SyncMappings = Default,
)

interface BankSyncSettingsLoader {
  suspend fun load(account: AccountId): BankSyncSettings
}

interface BankSyncSettingsWriter {
  suspend fun save(account: AccountId, settings: BankSyncSettings)
}
