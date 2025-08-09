package actual.budget.list.ui

import actual.budget.model.BudgetState
import actual.core.icons.ActualIcons
import actual.core.icons.CloudCheck
import actual.core.icons.CloudDownload
import actual.core.icons.CloudUnknown
import actual.core.icons.CloudWarning
import actual.core.icons.FileDouble
import actual.core.ui.LocalTheme
import actual.core.ui.Theme
import actual.l10n.Strings
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
  BudgetState.Local -> ActualIcons.FileDouble
  BudgetState.Remote -> ActualIcons.CloudDownload
  BudgetState.Synced -> ActualIcons.CloudCheck
  BudgetState.Syncing -> Icons.Filled.Sync
  BudgetState.Detached -> ActualIcons.CloudWarning
  BudgetState.Broken -> ActualIcons.CloudWarning
  BudgetState.Unknown -> ActualIcons.CloudUnknown
}

@Stable
@Composable
@ReadOnlyComposable
private fun BudgetState.iconColor(theme: Theme): Color = when (this) {
  BudgetState.Broken, BudgetState.Detached -> theme.warningText
  else -> theme.pageText
}
