package actual.budget.list.ui

import actual.budget.list.res.BudgetListStrings
import actual.budget.model.BudgetState
import actual.core.icons.ActualIcons
import actual.core.icons.CloudCheck
import actual.core.icons.CloudDownload
import actual.core.icons.CloudUnknown
import actual.core.icons.CloudWarning
import actual.core.icons.FileDouble
import actual.core.ui.ActualFontFamily
import actual.core.ui.HorizontalSpacer
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewActualRow
import actual.core.ui.Theme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.tooling.preview.Preview
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
      fontFamily = ActualFontFamily,
      color = theme.budgetItemTextSecondary,
      fontSize = 13.sp,
    )
  }
}

@Stable
@Composable
@ReadOnlyComposable
private fun BudgetState.text(): String = when (this) {
  BudgetState.Local -> BudgetListStrings.budgetStateLocal
  BudgetState.Remote -> BudgetListStrings.budgetStateRemote
  BudgetState.Synced -> BudgetListStrings.budgetStateSynced
  BudgetState.Syncing -> BudgetListStrings.budgetStateSyncing
  BudgetState.Detached -> BudgetListStrings.budgetStateDetached
  BudgetState.Broken -> BudgetListStrings.budgetStateBroken
  BudgetState.Unknown -> BudgetListStrings.budgetStateUnknown
}

@Stable
@Composable
@ReadOnlyComposable
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

@Preview
@Composable
private fun PreviewStates() = PreviewActualRow {
  LazyColumn {
    items(BudgetState.entries) {
      BudgetStateText(it)
    }
  }
}
