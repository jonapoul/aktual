package aktual.budget.reports.ui.dashboard

import aktual.budget.model.WidgetId
import aktual.budget.model.WidgetType
import aktual.budget.reports.ui.Action
import aktual.budget.reports.ui.ActionListener
import aktual.budget.reports.ui.charts.PER_TRANSACTION_DATA
import aktual.budget.reports.ui.charts.PREVIEW_CASH_FLOW_DATA
import aktual.budget.reports.ui.charts.PREVIEW_NET_WORTH_DATA
import aktual.budget.reports.ui.charts.ReportChart
import aktual.budget.reports.vm.dashboard.ReportDashboardItem
import aktual.core.icons.material.Delete
import aktual.core.icons.material.Edit
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.CardShape
import aktual.core.ui.LandscapePreview
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithTheme
import aktual.core.ui.ThemedDropdownMenu
import aktual.core.ui.ThemedDropdownMenuItem
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
    modifier =
      modifier
        .padding(4.dp)
        .fillMaxWidth()
        .wrapContentHeight()
        .background(theme.tableBackground, CardShape)
        .combinedClickable(
          onClick = { onAction(Action.OpenItem(item.id)) },
          onLongClick = { showContextMenu = true },
        )
        .padding(8.dp)
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
) {
  ThemedDropdownMenu(modifier = modifier, expanded = expanded, onDismissRequest = onDismiss) {
    ThemedDropdownMenuItem(
      text = { Text(Strings.reportsDashboardRename) },
      leadingIcon = { Icon(MaterialIcons.Edit, Strings.reportsDashboardRename) },
      onClick = {
        onDismiss()
        onAction(Action.Rename(item.id))
      },
    )
    ThemedDropdownMenuItem(
      text = { Text(Strings.reportsDashboardDelete) },
      leadingIcon = { Icon(MaterialIcons.Delete, Strings.reportsDashboardDelete) },
      onClick = {
        onDismiss()
        onAction(Action.Delete(item.id))
      },
    )
  }
}

@PortraitPreview
@LandscapePreview
@Composable
private fun PreviewReportDashboardItem(
  @PreviewParameter(ReportDashboardItemProvider::class) params: ThemedParams<ReportDashboardItem>
) =
  PreviewWithTheme(theme = params.theme) { ReportDashboardItem(item = params.data, onAction = {}) }

private class ReportDashboardItemProvider :
  ThemedParameterProvider<ReportDashboardItem>(
    PREVIEW_DASHBOARD_ITEM_1,
    PREVIEW_DASHBOARD_ITEM_2,
    PREVIEW_DASHBOARD_ITEM_3,
  )

internal val PREVIEW_DASHBOARD_ITEM_1 =
  ReportDashboardItem(
    id = WidgetId("abc-123"),
    type = WidgetType.CashFlow,
    data = PREVIEW_CASH_FLOW_DATA,
  )

internal val PREVIEW_DASHBOARD_ITEM_2 =
  ReportDashboardItem(
    id = WidgetId("def-456"),
    type = WidgetType.NetWorth,
    data = PREVIEW_NET_WORTH_DATA,
  )

internal val PREVIEW_DASHBOARD_ITEM_3 =
  ReportDashboardItem(
    id = WidgetId("xyz-789"),
    type = WidgetType.Summary,
    data = PER_TRANSACTION_DATA,
  )
