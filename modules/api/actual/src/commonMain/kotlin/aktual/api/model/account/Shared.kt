/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
@file:Suppress(
  "MatchingDeclarationName",
  "ktlint:standard:filename",
)

package aktual.api.model.account

import aktual.api.model.internal.BoolAsInt
import aktual.core.model.LoginMethod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AvailableLoginMethod(
  @SerialName("method") val method: LoginMethod,
  @SerialName("displayName") val displayName: String,
  @SerialName("active") val active: BoolAsInt,
)
