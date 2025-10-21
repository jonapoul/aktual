/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.about.vm

import androidx.compose.runtime.Immutable

@Immutable
sealed interface CheckUpdatesState {
  data object Inactive : CheckUpdatesState
  data object Checking : CheckUpdatesState
  data object NoUpdateFound : CheckUpdatesState
  data class Failed(val cause: String) : CheckUpdatesState
  data class UpdateFound(val version: String, val url: String) : CheckUpdatesState
}
