package aktual.api.client

import kotlinx.serialization.json.Json

val AktualJson = Json {
  encodeDefaults = true
  ignoreUnknownKeys = true
  explicitNulls = false
}
