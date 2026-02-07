package aktual.budget.prefs

import aktual.budget.model.DbMetadata
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
interface BudgetLocalPreferences : MutableStateFlow<DbMetadata> {
  operator fun <T : Any> get(key: DbMetadata.Key<T>): T? = value[key]
  operator fun <T : Any> set(key: DbMetadata.Key<T>, value: T?): DbMetadata = this.value.set(key, value)
  fun <T : Any> observe(key: DbMetadata.Key<T>): Flow<T?> = map { metadata -> metadata[key] }
}
