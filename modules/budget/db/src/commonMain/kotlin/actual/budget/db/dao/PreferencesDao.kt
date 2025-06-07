package actual.budget.db.dao

import actual.budget.db.BudgetDatabase
import actual.budget.db.PreferencesQueries
import actual.budget.db.asSingleNullableFlow
import actual.budget.db.withResult
import actual.budget.db.withoutResult
import actual.budget.model.SyncedPrefKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class PreferencesDao(private val database: BudgetDatabase) {
  suspend fun getAll(): Map<SyncedPrefKey, String?> = database
    .preferencesQueries
    .withResult {
      getAll()
        .executeAsList()
        .associate { (key, value) -> key to value }
    }

  suspend operator fun get(key: SyncedPrefKey): String? = database
    .preferencesQueries
    .withResult {
      getValue(key)
        .executeAsOneOrNull()
        ?.value_
    }

  suspend operator fun set(key: SyncedPrefKey, value: String?) = database
    .preferencesQueries
    .withoutResult {
      setValue(key, value?.toString())
    }

  suspend fun observe(key: SyncedPrefKey): Flow<String?> = database
    .preferencesQueries
    .withResult {
      getValue(key)
        .asSingleNullableFlow()
        .map { it?.value_?.toString() }
        .distinctUntilChanged()
    }

  suspend fun <R> withResult(
    query: suspend PreferencesQueries.() -> R,
  ): R = database.preferencesQueries.withResult(query)

  suspend fun withoutResult(
    query: suspend PreferencesQueries.() -> Unit,
  ) = database.preferencesQueries.withoutResult(query)
}
