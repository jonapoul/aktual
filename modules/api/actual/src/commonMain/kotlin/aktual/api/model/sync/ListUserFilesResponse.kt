/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.api.model.sync

import aktual.api.model.account.FailureReason
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface ListUserFilesResponse {
  @Serializable
  data class Success(
    @SerialName("data") val data: List<UserFile>,
  ) : ListUserFilesResponse

  @Serializable
  data class Failure(
    @SerialName("reason") val reason: FailureReason,
    @SerialName("details") val details: String? = null,
  ) : ListUserFilesResponse
}
