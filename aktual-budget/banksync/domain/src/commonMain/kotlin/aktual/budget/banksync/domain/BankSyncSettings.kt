package aktual.budget.banksync.domain

import aktual.budget.BudgetSyncController
import aktual.budget.db.dao.DatabaseTables.PREFERENCES
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.model.AccountId
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.SyncedPrefKey.PerAccount
import aktual.budget.model.SyncedPrefKey.PerAccount.CustomSyncMappings
import aktual.budget.model.SyncedPrefKey.PerAccount.SyncImportNotes
import aktual.budget.model.SyncedPrefKey.PerAccount.SyncImportPending
import aktual.budget.model.SyncedPrefKey.PerAccount.SyncImportTransactions
import aktual.budget.model.SyncedPrefKey.PerAccount.SyncReimportDeleted
import aktual.budget.model.SyncedPrefKey.PerAccount.SyncUpdateDates
import dev.zacsweers.metro.Inject

/**
 * An account's bank sync preferences, as packages/loot-core/src/server/accounts/sync.ts reads them.
 *
 * @property importNotes Fill in notes from the mapped field.
 * @property importPending Import transactions the bank hasn't booked yet, uncleared.
 * @property importTransactions Import anything at all after the first sync, rather than only
 *   updating the balance.
 * @property reimportDeleted Import a transaction again after it was deleted locally.
 * @property updateDates Overwrite the dates of matched transactions with the bank's.
 */
data class BankSyncSettings(
  val importNotes: Boolean = true,
  val importPending: Boolean = true,
  val importTransactions: Boolean = true,
  val reimportDeleted: Boolean = true,
  val updateDates: Boolean = false,
  val mappings: SyncMappings = SyncMappings.Default,
)

@Inject
class BankSyncSettingsLoader(private val preferences: PreferencesDao) {
  /** Throws [IllegalArgumentException] if the account's custom mappings aren't valid JSON. */
  suspend fun load(account: AccountId): BankSyncSettings =
    BankSyncSettings(
      importNotes = flag(SyncImportNotes(account), default = true),
      importPending = flag(SyncImportPending(account), default = true),
      importTransactions = flag(SyncImportTransactions(account), default = true),
      reimportDeleted = flag(SyncReimportDeleted(account), default = true),
      updateDates = flag(SyncUpdateDates(account), default = false),
      mappings =
        preferences[CustomSyncMappings(account)]
          ?.takeIf { it.isNotEmpty() }
          ?.let(SyncMappings::parse) ?: SyncMappings.Default,
    )

  // String(value ?? default) === 'true'
  private suspend fun flag(key: PerAccount, default: Boolean): Boolean =
    preferences[key]?.let { it == "true" } ?: default
}

/**
 * saveSettings() in packages/desktop-client/src/components/banksync/useBankSyncAccountSettings.ts.
 * They're synced preferences, so they go out as messages and reach the budget's other devices.
 */
@Inject
class BankSyncSettingsWriter(private val syncController: BudgetSyncController) {
  suspend fun save(account: AccountId, settings: BankSyncSettings) =
    syncController.syncChanges(
      listOf(
        change(CustomSyncMappings(account), settings.mappings.encode()),
        change(SyncImportPending(account), settings.importPending),
        change(SyncImportNotes(account), settings.importNotes),
        change(SyncReimportDeleted(account), settings.reimportDeleted),
        change(SyncImportTransactions(account), settings.importTransactions),
        change(SyncUpdateDates(account), settings.updateDates),
      )
    )

  private fun change(key: PerAccount, value: Boolean) = change(key, value.toString())

  private fun change(key: PerAccount, value: String) =
    LocalChange(PREFERENCES, key.key, column = "value", MessageValue.String(value))
}
