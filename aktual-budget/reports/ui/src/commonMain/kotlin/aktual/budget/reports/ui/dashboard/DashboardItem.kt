package aktual.budget.reports.ui.dashboard

import aktual.budget.reports.ui.Action
import aktual.budget.reports.ui.ActionListener
import aktual.budget.reports.ui.charts.PER_TRANSACTION_DATA
import aktual.budget.reports.ui.charts.PREVIEW_CASH_FLOW_DATA
import aktual.budget.reports.ui.charts.PREVIEW_NET_WORTH_DATA
import aktual.budget.reports.ui.charts.ReportChart
import aktual.budget.reports.vm.ChartData
import aktual.budget.reports.vm.dashboard.DashboardItem
import aktual.core.icons.material.Delete
import aktual.core.icons.material.Edit
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.CardShape
import aktual.core.ui.PreviewWithThemedParams
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.flowOf

@Composable
internal fun DashboardItem(
  item: DashboardItem,
  observer: DashboardItemObserver,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  var showContextMenu by remember { mutableStateOf(false) }

  val flow = remember(item) { observer(item) }
  val chartData by flow.collectAsStateWithLifecycle(initialValue = null)

  Box(
    modifier =
      modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .background(theme.tableBackground, CardShape)
        .combinedClickable(
          enabled = chartData != null,
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

    chartData?.let { data ->
      ReportChart(
        modifier = Modifier.fillMaxWidth(),
        data = data,
        compact = true,
        onAction = onAction,
      )
    }
  }
}

@Composable
private fun ReportDropDownMenu(
  item: DashboardItem,
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
        onAction(Action.Rename(item, name = "TODO: rename"))
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

@Preview
@Composable
private fun PreviewReportDashboardItem(
  @PreviewParameter(ReportDashboardItemProvider::class) params: ThemedParams<DashboardItemParams>
) =
  PreviewWithThemedParams(params) {
    DashboardItem(
      item = item,
      onAction = {},
      observer = { if (chartData == null) flowOf() else flowOf(chartData) },
    )
  }

private data class DashboardItemParams(val item: DashboardItem, val chartData: ChartData?)

private class ReportDashboardItemProvider :
  ThemedParameterProvider<DashboardItemParams>(
    DashboardItemParams(PREVIEW_DASHBOARD_ITEM_1, PREVIEW_CASH_FLOW_DATA),
    DashboardItemParams(PREVIEW_DASHBOARD_ITEM_2, PREVIEW_NET_WORTH_DATA),
    DashboardItemParams(PREVIEW_DASHBOARD_ITEM_3, PER_TRANSACTION_DATA),
    DashboardItemParams(PREVIEW_DASHBOARD_ITEM_3, null),
  )
