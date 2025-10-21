/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.api.client

import kotlinx.serialization.json.Json

val AktualJson = Json {
  encodeDefaults = true
  ignoreUnknownKeys = true
  explicitNulls = false
}
