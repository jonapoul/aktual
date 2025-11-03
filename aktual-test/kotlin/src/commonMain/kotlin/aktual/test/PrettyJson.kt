/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.test

import aktual.api.client.AktualJson
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
val PrettyJson = Json(from = AktualJson) {
  prettyPrint = true
  prettyPrintIndent = "  "
}
