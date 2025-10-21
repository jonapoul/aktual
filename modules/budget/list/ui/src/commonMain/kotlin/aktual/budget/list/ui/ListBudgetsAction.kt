/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.list.ui

import aktual.budget.model.Budget
import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface ListBudgetsAction {
  data object Reload : ListBudgetsAction
  data object ChangePassword : ListBudgetsAction
  data object ChangeServer : ListBudgetsAction
  data object OpenAbout : ListBudgetsAction
  data object OpenInBrowser : ListBudgetsAction
  data object OpenSettings : ListBudgetsAction
  data class Open(val budget: Budget) : ListBudgetsAction
  data class Delete(val budget: Budget) : ListBudgetsAction
}
