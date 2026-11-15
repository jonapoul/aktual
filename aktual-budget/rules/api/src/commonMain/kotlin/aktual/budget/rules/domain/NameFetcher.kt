package aktual.budget.rules.domain

import aktual.budget.model.Field
import aktual.budget.model.ScheduleId
import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonArray

@Stable
interface NameFetcher {
  fun name(field: Field, id: String): Flow<String?>

  fun name(id: ScheduleId): Flow<String?>

  fun names(field: Field, ids: List<String>): Flow<JsonArray>
}
