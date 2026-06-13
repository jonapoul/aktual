package aktual.test

import aktual.core.model.AktualJson
import kotlinx.serialization.json.Json

val PrettyJson =
  Json(from = AktualJson) {
    prettyPrint = true
    prettyPrintIndent = "  "
  }
