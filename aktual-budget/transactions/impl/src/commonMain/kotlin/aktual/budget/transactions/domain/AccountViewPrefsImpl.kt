package aktual.budget.transactions.domain

import aktual.budget.db.dao.DatabaseTables.PREFERENCES
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.model.AccountId
import aktual.budget.model.SyncedPrefKey
import aktual.budget.model.SyncedPrefKey.PerAccount.ShowBalances
import aktual.budget.model.SyncedPrefKey.PerAccount.TransactionTableColumns
import aktual.budget.model.localChange
import aktual.budget.utils.BudgetSync
import aktual.di.BudgetScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Inject
@ContributesBinding(BudgetScope::class)
class AccountViewPrefsImpl
internal constructor(
  private val preferences: PreferencesDao,
  private val budgetSync: BudgetSync,
) : AccountViewPrefs {
  override fun observe(key: SyncedPrefKey.PerAccount): Flow<Boolean> =
    preferences.observe(key).map { it == "true" }

  override suspend fun set(key: SyncedPrefKey.PerAccount, enabled: Boolean) =
    write(key, enabled.toString())

  override fun observeRunningBalance(account: AccountId): Flow<Boolean> =
    combine(
        preferences.observe(TransactionTableColumns(account)),
        preferences.observe(SyncedPrefKey.Global.TransactionTableColumns),
        preferences.observe(ShowBalances(account)),
      ) { view, global, legacy ->
        runningBalance(view, global, legacy)
      }
      .distinctUntilChanged()

  // With no columns saved, upstream still reads show-balances. Saving columns then would start
  // ignoring its other old column prefs too, so only that one's written
  override suspend fun setRunningBalance(account: AccountId, enabled: Boolean) {
    val columns = columnsConfig(account)
    if (columns == null) {
      set(ShowBalances(account), enabled)
      return
    }
    val updated =
      parseTransactionTableColumns(columns).map { column ->
        if (column.id == BALANCE_COLUMN) column.copy(hidden = !enabled) else column
      }
    write(TransactionTableColumns(account), updated.encode())
  }

  // useTransactionTableColumns(): the view's columns win over the budget's. Upstream clears a
  // view's by saving an empty string
  private suspend fun columnsConfig(account: AccountId): String? =
    preferences[TransactionTableColumns(account)].orNullIfEmpty()
      ?: preferences[SyncedPrefKey.Global.TransactionTableColumns].orNullIfEmpty()

  private fun runningBalance(view: String?, global: String?, legacy: String?): Boolean {
    val columns = view.orNullIfEmpty() ?: global.orNullIfEmpty() ?: return legacy == "true"
    return parseTransactionTableColumns(columns).none { it.id == BALANCE_COLUMN && it.hidden }
  }

  private suspend fun write(key: SyncedPrefKey, value: String) =
    budgetSync(localChange(PREFERENCES, key.key, column = "value", value))

  private fun String?.orNullIfEmpty(): String? = this?.ifEmpty { null }
}
