package aktual.budget

import aktual.budget.model.DbMetadata
import aktual.budget.model.DbMetadataKey
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
interface BudgetLocalPreferences : MutableStateFlow<DbMetadata> {
  operator fun <T : Any> get(key: DbMetadataKey<T>): T? = value[key]

  operator fun <T : Any> set(key: DbMetadataKey<T>, value: T?): DbMetadata =
    this.value.set(key, value)

  operator fun plusAssign(metadata: DbMetadata) = update { previous -> previous + metadata }

  fun <T : Any> observe(key: DbMetadataKey<T>): Flow<T?> = map { metadata -> metadata[key] }
}
