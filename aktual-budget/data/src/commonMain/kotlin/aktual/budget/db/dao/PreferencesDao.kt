package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.PreferencesQueries
import aktual.budget.db.withResult
import aktual.budget.db.withoutResult
import aktual.budget.model.SyncedPrefKey
import alakazam.kotlin.CoroutineContexts
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Inject
class PreferencesDao(
    database: BudgetDatabase,
    private val contexts: CoroutineContexts,
) {
  private val queries = database.preferencesQueries

  suspend fun getAll(): Map<SyncedPrefKey, String?> = withResult {
    getAll().executeAsList().associate { (key, value) -> key to value }
  }

  suspend operator fun get(key: SyncedPrefKey): String? = withResult {
    getValue(key).executeAsOneOrNull()?.value_
  }

  suspend operator fun set(key: SyncedPrefKey, value: String?) = withoutResult {
    setValue(key, value)
  }

  fun observe(key: SyncedPrefKey): Flow<String?> =
      queries
          .getValue(key)
          .asFlow()
          .mapToOneOrNull(contexts.default)
          .map { it?.value_ }
          .distinctUntilChanged()

  suspend fun <R> withResult(
      query: suspend PreferencesQueries.() -> R,
  ): R = queries.withResult(query)

  suspend fun withoutResult(
      query: suspend PreferencesQueries.() -> Unit,
  ) = queries.withoutResult(query)
}
