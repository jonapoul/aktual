/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.api.model.base

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InfoResponse(
  @SerialName("build") val build: Build,
)

@Serializable
data class Build(
  @SerialName("name") val name: String,
  @SerialName("description") val description: String,
  @SerialName("version") val version: String,
)
