package actual.budget.db.dao

import actual.budget.db.BudgetDatabase
import actual.budget.db.PreferencesQueries
import actual.budget.db.withResult
import actual.budget.db.withoutResult
import actual.budget.model.SyncedPrefKey
import alakazam.kotlin.core.CoroutineContexts
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PreferencesDao @Inject constructor(
  database: BudgetDatabase,
  private val contexts: CoroutineContexts,
) {
  private val queries = database.preferencesQueries

  suspend fun getAll(): Map<SyncedPrefKey, String?> = queries.withResult {
    getAll()
      .executeAsList()
      .associate { (key, value) -> key to value }
  }

  suspend operator fun get(key: SyncedPrefKey): String? = queries.withResult {
    getValue(key)
      .executeAsOneOrNull()
      ?.value_
  }

  suspend operator fun set(key: SyncedPrefKey, value: String?) = queries.withoutResult {
    setValue(key, value?.toString())
  }

  fun observe(key: SyncedPrefKey): Flow<String?> = queries
    .getValue(key)
    .asFlow()
    .mapToOneOrNull(contexts.default)
    .map { it?.value_?.toString() }
    .distinctUntilChanged()

  suspend fun <R> withResult(
    query: suspend PreferencesQueries.() -> R,
  ): R = queries.withResult(query)

  suspend fun withoutResult(
    query: suspend PreferencesQueries.() -> Unit,
  ) = queries.withoutResult(query)
}
