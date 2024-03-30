package dev.jonpoulton.actual.listbudgets.ui

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jonpoulton.actual.core.icons.ActualIcons
import dev.jonpoulton.actual.core.icons.CloudCheck
import dev.jonpoulton.actual.core.icons.CloudDownload
import dev.jonpoulton.actual.core.icons.CloudUnknown
import dev.jonpoulton.actual.core.icons.CloudWarning
import dev.jonpoulton.actual.core.icons.FileDouble
import dev.jonpoulton.actual.core.ui.ActualColorScheme
import dev.jonpoulton.actual.core.ui.ActualFontFamily
import dev.jonpoulton.actual.core.ui.HorizontalSpacer
import dev.jonpoulton.actual.core.ui.LocalActualColorScheme
import dev.jonpoulton.actual.core.ui.PreviewActualRow
import dev.jonpoulton.actual.listbudgets.vm.BudgetState
import dev.jonpoulton.actual.core.res.R as ResR

@Stable
@Composable
internal fun BudgetStateText(
  state: BudgetState,
  modifier: Modifier = Modifier,
  colors: ActualColorScheme = LocalActualColorScheme.current,
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    val text = stateText(state = state)

    Icon(
      modifier = Modifier.size(18.dp),
      imageVector = stateIcon(state),
      contentDescription = text,
      tint = colors.stateIconColor(state),
    )

    HorizontalSpacer(5.dp)

    Text(
      text = text,
      fontFamily = ActualFontFamily,
      color = colors.pageText,
      fontSize = 13.sp,
    )
  }
}

@Stable
@Composable
@ReadOnlyComposable
private fun stateText(state: BudgetState): String = when (state) {
  BudgetState.Local -> stringResource(id = ResR.string.budget_state_local)
  BudgetState.Remote -> stringResource(id = ResR.string.budget_state_remote)
  BudgetState.Synced -> stringResource(id = ResR.string.budget_state_synced)
  BudgetState.Syncing -> stringResource(id = ResR.string.budget_state_syncing)
  BudgetState.Detached -> stringResource(id = ResR.string.budget_state_detached)
  BudgetState.Broken -> stringResource(id = ResR.string.budget_state_broken)
  BudgetState.Unknown -> stringResource(id = ResR.string.budget_state_unknown)
}

@Stable
@Composable
@ReadOnlyComposable
private fun stateIcon(state: BudgetState): ImageVector = when (state) {
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
private fun ActualColorScheme.stateIconColor(state: BudgetState): Color = when (state) {
  BudgetState.Broken, BudgetState.Detached -> warningText
  else -> pageText
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
