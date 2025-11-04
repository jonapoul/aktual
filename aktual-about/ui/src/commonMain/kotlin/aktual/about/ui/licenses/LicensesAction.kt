/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.about.ui.licenses

import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface LicensesAction {
  data object NavBack : LicensesAction
  data object Reload : LicensesAction
  data object ToggleSearchBar : LicensesAction
  data class EditSearchText(val text: String) : LicensesAction
  data class LaunchUrl(val url: String) : LicensesAction
}
