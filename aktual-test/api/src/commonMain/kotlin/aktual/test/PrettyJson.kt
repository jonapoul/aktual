package aktual.test

import aktual.core.model.AktualJson
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
val PrettyJson =
  Json(from = AktualJson) {
    prettyPrint = true
    prettyPrintIndent = "  "
  }
