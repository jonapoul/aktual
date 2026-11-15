package aktual.budget.rules.domain

import aktual.budget.model.RecurConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

private val RecurConfigJson = Json { ignoreUnknownKeys = true }

// A recurring date condition's value, or null if it isn't one
fun JsonElement.toRecurConfigOrNull(): RecurConfig? {
  if (this !is JsonObject) return null
  val config =
    try {
      RecurConfigJson.decodeFromJsonElement(RecurConfig.serializer(), this)
    } catch (_: IllegalArgumentException) {
      return null
    }
  return config.takeIf { it.frequency != Unknown }
}

fun RecurConfig.toJsonElement(): JsonElement =
  RecurConfigJson.encodeToJsonElement(RecurConfig.serializer(), this)
