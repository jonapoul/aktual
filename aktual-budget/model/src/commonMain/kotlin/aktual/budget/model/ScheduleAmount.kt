package aktual.budget.model

import kotlin.math.floor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

// packages/loot-core/src/shared/schedules.ts getScheduledAmount(). _amount is either a number or
// an {"num1":..,"num2":..} object for "is between" amounts, which uses the average
fun parseScheduleAmount(raw: String?): Long? {
  raw ?: return null
  raw.toLongOrNull()?.let {
    return it
  }
  return runCatching {
    val obj = Json.parseToJsonElement(raw).jsonObject
    val num1 = obj.getValue("num1").jsonPrimitive.long
    val num2 = obj.getValue("num2").jsonPrimitive.long
    // Math.round() rounds halves up
    floor((num1 + num2).toDouble() / 2 + HALF).toLong()
  }
    .getOrNull()
}

private const val HALF = 0.5
