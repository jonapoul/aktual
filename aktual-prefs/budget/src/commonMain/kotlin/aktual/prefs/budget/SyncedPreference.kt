package aktual.prefs.budget

import aktual.budget.db.dao.DatabaseTables.PREFERENCES
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.model.SyncedPrefKey
import aktual.budget.model.localChange
import aktual.budget.utils.BudgetSync
import aktual.prefs.Preference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class SyncedPreference<T : Any>(
  private val dao: PreferencesDao,
  private val budgetSync: BudgetSync,
  private val key: SyncedPrefKey,
  override val default: T,
  private val decode: (String) -> T?,
  private val encode: (T) -> String,
) : Preference<T> {
  override suspend fun get(): T = parse(dao[key])

  override suspend fun set(value: T?) {
    budgetSync(localChange(PREFERENCES, key.key, column = "value", value?.let(encode)))
  }

  override fun asFlow(): Flow<T> = dao.observe(key).map(::parse).distinctUntilChanged()

  private fun parse(value: String?): T = value?.let(decode) ?: default
}

internal fun booleanPref(
  dao: PreferencesDao,
  budgetSync: BudgetSync,
  key: SyncedPrefKey,
  default: Boolean,
): Preference<Boolean> =
  SyncedPreference(dao, budgetSync, key, default, String::toBooleanStrictOrNull, Boolean::toString)
