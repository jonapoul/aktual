package aktual.budget.rules.ui

import aktual.budget.model.Field
import aktual.budget.model.ScheduleId
import aktual.budget.rules.vm.NameFetcher
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray

internal val LocalNameFetcher = staticCompositionLocalOf<NameFetcher> { DummyNameFetcher }

// False while the rule editor is saving or deleting, so its inputs can't change underneath it
internal val LocalRuleEditable = compositionLocalOf { true }

@Suppress("StringLiteralDuplication")
internal object DummyNameFetcher : NameFetcher {
  override fun name(field: Field, id: String): Flow<String?> = flowOf("Dummy")

  override fun name(id: ScheduleId): Flow<String?> = flowOf("Dummy")

  override fun names(field: Field, ids: List<String>): Flow<JsonArray> =
    flowOf(buildJsonArray { add(JsonPrimitive("Dummy")) })
}
