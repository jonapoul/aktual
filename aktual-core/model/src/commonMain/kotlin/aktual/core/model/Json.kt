package aktual.core.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

val JsonObject.Companion.Empty: JsonObject
  get() = JsonObject(emptyMap())

val AktualJson = Json {
  encodeDefaults = true
  ignoreUnknownKeys = true
  explicitNulls = false
}
