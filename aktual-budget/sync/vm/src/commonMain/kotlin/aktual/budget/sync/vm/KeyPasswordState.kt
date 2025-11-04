/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.sync.vm

import aktual.core.model.Password

sealed interface KeyPasswordState {
  data class Active(val input: Password) : KeyPasswordState
  data object Inactive : KeyPasswordState
}
