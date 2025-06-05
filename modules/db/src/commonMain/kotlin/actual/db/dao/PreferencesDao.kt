package actual.db.dao

import actual.budget.model.SyncedPrefKey
import actual.db.BudgetDatabase
import actual.db.PreferencesQueries
import actual.db.asSingleNullableFlow
import actual.db.withResult
import actual.db.withoutResult
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
