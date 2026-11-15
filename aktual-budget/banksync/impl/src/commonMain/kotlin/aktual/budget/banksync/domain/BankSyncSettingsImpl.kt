package aktual.budget.banksync.domain

import aktual.budget.db.dao.DatabaseTables.PREFERENCES
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.model.AccountId
import aktual.budget.model.SyncedPrefKey.PerAccount
import aktual.budget.model.SyncedPrefKey.PerAccount.CustomSyncMappings
import aktual.budget.model.SyncedPrefKey.PerAccount.SyncImportNotes
import aktual.budget.model.SyncedPrefKey.PerAccount.SyncImportPending
import aktual.budget.model.SyncedPrefKey.PerAccount.SyncImportTransactions
import aktual.budget.model.SyncedPrefKey.PerAccount.SyncReimportDeleted
import aktual.budget.model.SyncedPrefKey.PerAccount.SyncUpdateDates
import aktual.budget.model.localChange
import aktual.budget.utils.BudgetSync
import aktual.di.BudgetScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(BudgetScope::class)
class BankSyncSettingsLoaderImpl(private val preferences: PreferencesDao) : BankSyncSettingsLoader {
  override suspend fun load(account: AccountId): BankSyncSettings =
    BankSyncSettings(
      importNotes = flag(SyncImportNotes(account), default = true),
      importPending = flag(SyncImportPending(account), default = true),
      importTransactions = flag(SyncImportTransactions(account), default = true),
      reimportDeleted = flag(SyncReimportDeleted(account), default = true),
      updateDates = flag(SyncUpdateDates(account), default = false),
      mappings =
        preferences[CustomSyncMappings(account)]
          ?.takeIf { it.isNotEmpty() }
          ?.let { SyncMappings.parse(it) } ?: Default,
    )

  // String(value ?? default) === 'true'
  private suspend fun flag(key: PerAccount, default: Boolean): Boolean =
    preferences[key]?.let { it == "true" } ?: default
}

@Inject
@ContributesBinding(BudgetScope::class)
class BankSyncSettingsWriterImpl(private val budgetSync: BudgetSync) : BankSyncSettingsWriter {
  override suspend fun save(account: AccountId, settings: BankSyncSettings) {
    budgetSync(
      listOf(
        change(CustomSyncMappings(account), settings.mappings.encode()),
        change(SyncImportPending(account), settings.importPending),
        change(SyncImportNotes(account), settings.importNotes),
        change(SyncReimportDeleted(account), settings.reimportDeleted),
        change(SyncImportTransactions(account), settings.importTransactions),
        change(SyncUpdateDates(account), settings.updateDates),
      ),
    )
  }

  private fun change(key: PerAccount, value: Boolean) = change(key, value.toString())

  private fun change(key: PerAccount, value: String) =
    localChange(PREFERENCES, key.key, column = "value", value)
}
