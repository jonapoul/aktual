package aktual.budget.rules.domain

import aktual.budget.model.Field
import aktual.budget.model.ScheduleId
import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonArray

@Stable
interface NameFetcher {
  operator fun invoke(field: Field, id: String): Flow<String?>

  operator fun invoke(id: ScheduleId): Flow<String?>

  operator fun invoke(field: Field, ids: List<String>): Flow<JsonArray>
}
