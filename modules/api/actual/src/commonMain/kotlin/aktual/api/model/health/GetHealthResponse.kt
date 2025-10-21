/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.api.model.health

import kotlinx.serialization.Serializable

@Serializable
data class GetHealthResponse(
  val status: String = "UP",
)
