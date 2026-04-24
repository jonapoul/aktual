package aktual.budget.db.dao

import kotlinx.serialization.json.JsonObject

data class ApplyResult(val merkle: JsonObject, val affectedTables: Set<String>)
