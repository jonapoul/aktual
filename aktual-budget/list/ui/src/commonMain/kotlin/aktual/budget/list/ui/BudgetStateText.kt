package aktual.budget.list.ui

import aktual.budget.model.BudgetState
import aktual.core.icons.AktualIcons
import aktual.core.icons.CloudCheck
import aktual.core.icons.CloudDownload
import aktual.core.icons.CloudUnknown
import aktual.core.icons.CloudWarning
import aktual.core.icons.FileDouble
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColors
import alakazam.compose.HorizontalSpacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun BudgetStateText(
  state: BudgetState,
  modifier: Modifier = Modifier,
) {
  Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
    val text = state.text()
    val color = state.color(colors)

    Icon(
      modifier = Modifier.size(18.dp),
      imageVector = state.icon(),
      contentDescription = text,
      tint = color,
    )

    HorizontalSpacer(5.dp)

    Text(text = text, color = color, fontSize = 13.sp)
  }
}

@Composable
private fun BudgetState.text(): String =
  when (this) {
    Local -> Strings.budgetStateLocal
    Remote -> Strings.budgetStateRemote
    Synced -> Strings.budgetStateSynced
    Detached -> Strings.budgetStateDetached
    Broken -> Strings.budgetStateBroken
    Unknown -> Strings.budgetStateUnknown
  }

@Stable
private fun BudgetState.icon(): ImageVector =
  when (this) {
    Local -> AktualIcons.FileDouble
    Remote -> AktualIcons.CloudDownload
    Synced -> AktualIcons.CloudCheck
    Detached -> AktualIcons.CloudWarning
    Broken -> AktualIcons.CloudWarning
    Unknown -> AktualIcons.CloudUnknown
  }

@Stable
private fun BudgetState.color(colors: Colors): Color =
  when (this) {
    Broken -> colors.errorText
    Detached -> colors.warningText
    Local -> colors.pageTextPositive
    Remote -> colors.reportsBlue
    Synced -> colors.reportsGreen
    Unknown -> colors.reportsGray
  }

@Preview
@Composable
private fun PreviewBudgetStateText(
  @PreviewParameter(BudgetStateProvider::class) params: ColoredParams<BudgetState>
) = PreviewWithColors(params.colors) { BudgetStateText(params.data) }

private class BudgetStateProvider : ColoredParameterProvider<BudgetState>(BudgetState.entries)
