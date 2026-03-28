package aktual.budget.list.ui

import aktual.budget.model.BudgetState
import aktual.core.icons.AktualIcons
import aktual.core.icons.CloudCheck
import aktual.core.icons.CloudDownload
import aktual.core.icons.CloudUnknown
import aktual.core.icons.CloudWarning
import aktual.core.icons.FileDouble
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Sync
import aktual.core.l10n.Strings
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.PreviewWithTheme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import alakazam.compose.HorizontalSpacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Stable
@Composable
internal fun BudgetStateText(
  state: BudgetState,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
    val text = state.text()

    Icon(
      modifier = Modifier.size(18.dp),
      imageVector = state.icon(),
      contentDescription = text,
      tint = state.iconColor(theme),
    )

    HorizontalSpacer(5.dp)

    Text(text = text, color = theme.pageText, fontSize = 13.sp)
  }
}

@Composable
private fun BudgetState.text(): String =
  when (this) {
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
private fun BudgetState.icon(): ImageVector =
  when (this) {
    BudgetState.Local -> AktualIcons.FileDouble
    BudgetState.Remote -> AktualIcons.CloudDownload
    BudgetState.Synced -> AktualIcons.CloudCheck
    BudgetState.Syncing -> MaterialIcons.Sync
    BudgetState.Detached -> AktualIcons.CloudWarning
    BudgetState.Broken -> AktualIcons.CloudWarning
    BudgetState.Unknown -> AktualIcons.CloudUnknown
  }

@Stable
@Composable
@ReadOnlyComposable
@Suppress("ElseCaseInsteadOfExhaustiveWhen")
private fun BudgetState.iconColor(theme: Theme): Color =
  when (this) {
    BudgetState.Broken,
    BudgetState.Detached -> theme.warningText
    else -> theme.pageText
  }

@Preview
@Composable
private fun PreviewBudgetStateText(
  @PreviewParameter(BudgetStateProvider::class) params: ThemedParams<BudgetState>
) = PreviewWithTheme(params.theme) { BudgetStateText(params.data) }

private class BudgetStateProvider : ThemedParameterProvider<BudgetState>(BudgetState.entries)
