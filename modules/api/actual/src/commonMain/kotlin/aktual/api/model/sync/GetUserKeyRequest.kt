/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.api.model.sync

import aktual.budget.model.BudgetId
import aktual.core.model.LoginToken
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetUserKeyRequest(
  @SerialName("fileId") val id: BudgetId,
  @SerialName("token") val token: LoginToken,
)
