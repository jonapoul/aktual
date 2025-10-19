/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package aktual.budget.list.ui

import aktual.budget.model.BudgetState
import aktual.core.icons.AktualIcons
import aktual.core.icons.CloudCheck
import aktual.core.icons.CloudDownload
import aktual.core.icons.CloudUnknown
import aktual.core.icons.CloudWarning
import aktual.core.icons.FileDouble
import aktual.core.ui.LocalTheme
import aktual.core.ui.Theme
import aktual.l10n.Strings
import alakazam.kotlin.compose.HorizontalSpacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Stable
@Composable
internal fun BudgetStateText(
  state: BudgetState,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    val text = state.text()

    Icon(
      modifier = Modifier.size(18.dp),
      imageVector = state.icon(),
      contentDescription = text,
      tint = state.iconColor(theme),
    )

    HorizontalSpacer(5.dp)

    Text(
      text = text,
      color = theme.budgetItemTextSecondary,
      fontSize = 13.sp,
    )
  }
}

@Composable
private fun BudgetState.text(): String = when (this) {
  BudgetState.Local -> Strings.budgetStateLocal
  BudgetState.Remote -> Strings.budgetStateRemote
  BudgetState.Synced -> Strings.budgetStateSynced
  BudgetState.Syncing -> Strings.budgetStateSyncing
  BudgetState.Detached -> Strings.budgetStateDetached
  BudgetState.Broken -> Strings.budgetStateBroken
  BudgetState.Unknown -> Strings.budgetStateUnknown
}

@Stable
@Composable
private fun BudgetState.icon(): ImageVector = when (this) {
  BudgetState.Local -> AktualIcons.FileDouble
  BudgetState.Remote -> AktualIcons.CloudDownload
  BudgetState.Synced -> AktualIcons.CloudCheck
  BudgetState.Syncing -> Icons.Filled.Sync
  BudgetState.Detached -> AktualIcons.CloudWarning
  BudgetState.Broken -> AktualIcons.CloudWarning
  BudgetState.Unknown -> AktualIcons.CloudUnknown
}

@Stable
@Composable
@ReadOnlyComposable
private fun BudgetState.iconColor(theme: Theme): Color = when (this) {
  BudgetState.Broken, BudgetState.Detached -> theme.warningText
  else -> theme.pageText
}
