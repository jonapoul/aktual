package actual.budget.reports.ui

import actual.budget.reports.ui.charts.ReportChart
import actual.budget.reports.vm.ReportDashboardItem
import actual.core.ui.CardShape
import actual.core.ui.LocalTheme
import actual.core.ui.Theme
import actual.l10n.Strings
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun ReportDashboardItem(
  item: ReportDashboardItem,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  var showContextMenu by remember { mutableStateOf(false) }

  Box(
    modifier = modifier
      .padding(4.dp)
      .fillMaxWidth()
      .wrapContentHeight()
      .background(theme.tableBackground, CardShape)
      .combinedClickable(
        onClick = { onAction(Action.OpenItem(item.id)) },
        onLongClick = { showContextMenu = true },
      ).padding(8.dp),
  ) {
    ReportDropDownMenu(
      item = item,
      expanded = showContextMenu,
      onDismiss = { showContextMenu = false },
      onAction = onAction,
    )

    ReportChart(
      modifier = Modifier.fillMaxWidth(),
      data = item.data,
      compact = true,
      onAction = onAction,
      theme = theme,
    )
  }
}

@Composable
private fun ReportDropDownMenu(
  item: ReportDashboardItem,
  expanded: Boolean,
  onDismiss: () -> Unit,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
) = DropdownMenu(
  modifier = modifier,
  expanded = expanded,
  onDismissRequest = onDismiss,
) {
  DropdownMenuItem(
    text = { Text(Strings.reportsDashboardRename) },
    leadingIcon = { Icon(Icons.Filled.Edit, Strings.reportsDashboardRename) },
    onClick = {
      onDismiss()
      onAction(Action.Rename(item.id))
    },
  )
  DropdownMenuItem(
    text = { Text(Strings.reportsDashboardDelete) },
    leadingIcon = { Icon(Icons.Filled.Delete, Strings.reportsDashboardDelete) },
    onClick = {
      onDismiss()
      onAction(Action.Delete(item.id))
    },
  )
}
