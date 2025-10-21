/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.core.model

import kotlinx.serialization.json.JsonObject

val JsonObject.Companion.Empty: JsonObject
  get() = JsonObject(emptyMap())
