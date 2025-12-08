package aktual.core.model

import kotlinx.serialization.json.JsonObject

val JsonObject.Companion.Empty: JsonObject
  get() = JsonObject(emptyMap())
