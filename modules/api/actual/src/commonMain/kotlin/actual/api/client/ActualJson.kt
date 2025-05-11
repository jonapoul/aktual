package actual.api.client

import kotlinx.serialization.json.Json

val ActualJson = Json {
  encodeDefaults = true
  ignoreUnknownKeys = true
  explicitNulls = false
}
