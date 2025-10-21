/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.about.data

sealed interface LicensesLoadState {
  data class Success(val libraries: List<ArtifactDetail>) : LicensesLoadState
  data class Failure(val cause: String) : LicensesLoadState
}
