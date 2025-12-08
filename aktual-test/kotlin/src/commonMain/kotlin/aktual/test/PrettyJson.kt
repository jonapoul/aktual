package aktual.test

import aktual.api.client.AktualJson
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
val PrettyJson = Json(from = AktualJson) {
  prettyPrint = true
  prettyPrintIndent = "  "
}
