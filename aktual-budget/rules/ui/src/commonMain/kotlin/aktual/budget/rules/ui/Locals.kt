package aktual.budget.rules.ui

import aktual.budget.model.Field
import aktual.budget.model.ScheduleId
import aktual.budget.rules.vm.EntityListFetcher
import aktual.budget.rules.vm.EntitySummary
import aktual.budget.rules.vm.NameFetcher
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray

internal val LocalNameFetcher = staticCompositionLocalOf<NameFetcher> { DummyNameFetcher }

internal val LocalEntityListFetcher =
  staticCompositionLocalOf<EntityListFetcher> { DummyEntityListFetcher }

@Suppress("StringLiteralDuplication")
internal object DummyNameFetcher : NameFetcher {
  override fun name(field: Field, id: String): Flow<String?> = flowOf("Dummy")

  override fun name(id: ScheduleId): Flow<String?> = flowOf("Dummy")

  override fun names(field: Field, ids: List<String>): Flow<JsonArray> =
    flowOf(buildJsonArray { add(JsonPrimitive("Dummy")) })
}

internal object DummyEntityListFetcher : EntityListFetcher {
  override suspend fun payees(): ImmutableList<EntitySummary> = persistentListOf()

  override suspend fun accounts(): ImmutableList<EntitySummary> = persistentListOf()

  override suspend fun categories(): ImmutableList<EntitySummary> = persistentListOf()

  override suspend fun categoryGroups(): ImmutableList<EntitySummary> = persistentListOf()
}
