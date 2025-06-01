package actual.test

import actual.api.client.ActualJson
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
val PrettyJson = Json(from = ActualJson) {
  prettyPrint = true
  prettyPrintIndent = "  "
}
