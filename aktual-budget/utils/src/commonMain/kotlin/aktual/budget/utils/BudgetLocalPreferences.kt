package aktual.budget.utils

import aktual.budget.model.DbMetadata
import aktual.budget.model.DbMetadataKey
import aktual.core.utils.mapState
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
interface BudgetLocalPreferences : MutableStateFlow<DbMetadata> {
  operator fun <T : Any> get(key: DbMetadataKey<T>): T? = value[key]

  operator fun <T : Any> set(key: DbMetadataKey<T>, value: T?): DbMetadata =
    this.value.set(key, value)

  operator fun plusAssign(metadata: DbMetadata) = update { previous -> previous + metadata }

  fun <T : Any> observe(key: DbMetadataKey<T>): StateFlow<T?> = mapState { metadata -> metadata[key] }
}
