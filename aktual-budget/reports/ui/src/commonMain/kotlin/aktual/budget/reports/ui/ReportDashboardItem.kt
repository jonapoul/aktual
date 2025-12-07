/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.reports.ui

import aktual.budget.model.WidgetId
import aktual.budget.reports.ui.charts.PER_TRANSACTION_DATA
import aktual.budget.reports.ui.charts.PREVIEW_CASH_FLOW_DATA
import aktual.budget.reports.ui.charts.PREVIEW_NET_WORTH_DATA
import aktual.budget.reports.ui.charts.ReportChart
import aktual.budget.reports.vm.ReportDashboardItem
import aktual.core.ui.CardShape
import aktual.core.ui.LocalTheme
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.Theme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.l10n.Strings
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
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

@Preview
@Composable
private fun PreviewReportDashboardItem(
  @PreviewParameter(ReportDashboardItemProvider::class) params: ThemedParams<ReportDashboardItem>,
) = PreviewWithColorScheme(schemeType = params.type) {
  ReportDashboardItem(
    item = params.data,
    onAction = {},
  )
}

private class ReportDashboardItemProvider : ThemedParameterProvider<ReportDashboardItem>(
  PREVIEW_DASHBOARD_ITEM_1,
  PREVIEW_DASHBOARD_ITEM_2,
  PREVIEW_DASHBOARD_ITEM_3,
)

internal val PREVIEW_DASHBOARD_ITEM_1 = ReportDashboardItem(
  id = WidgetId("abc-123"),
  name = "Pensions",
  data = PREVIEW_CASH_FLOW_DATA,
)

internal val PREVIEW_DASHBOARD_ITEM_2 = ReportDashboardItem(
  id = WidgetId("def-456"),
  name = "Groceries",
  data = PREVIEW_NET_WORTH_DATA,
)

internal val PREVIEW_DASHBOARD_ITEM_3 = ReportDashboardItem(
  id = WidgetId("xyz-789"),
  name = "Pensions",
  data = PER_TRANSACTION_DATA,
)
