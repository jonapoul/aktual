package aktual.budget.rules.ui

import aktual.budget.model.Field
import aktual.budget.model.ScheduleId
import aktual.budget.rules.domain.NameFetcher
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray

internal val LocalNameFetcher = staticCompositionLocalOf<NameFetcher> { DummyNameFetcher }

internal object DummyNameFetcher : NameFetcher {
  override fun invoke(field: Field, id: String): Flow<String?> = flowOf("Dummy")

  override fun invoke(id: ScheduleId): Flow<String?> = flowOf("Dummy")

  override fun invoke(field: Field, ids: List<String>): Flow<JsonArray> =
    flowOf(buildJsonArray { add(JsonPrimitive("Dummy")) })
}

// Never emits, so names stay loading
internal object LoadingNameFetcher : NameFetcher {
  override fun invoke(field: Field, id: String): Flow<String?> = flow { awaitCancellation() }

  override fun invoke(id: ScheduleId): Flow<String?> = flow { awaitCancellation() }

  override fun invoke(field: Field, ids: List<String>): Flow<JsonArray> = flow {
    awaitCancellation()
  }
}
