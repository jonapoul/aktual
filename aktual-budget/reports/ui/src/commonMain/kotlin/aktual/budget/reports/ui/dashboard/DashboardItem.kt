package aktual.budget.reports.ui.dashboard

import aktual.budget.reports.ui.Action
import aktual.budget.reports.ui.ActionListener
import aktual.budget.reports.ui.charts.PER_TRANSACTION_DATA
import aktual.budget.reports.ui.charts.PREVIEW_CASH_FLOW_DATA
import aktual.budget.reports.ui.charts.PREVIEW_NET_WORTH_DATA
import aktual.budget.reports.ui.charts.ReportChart
import aktual.budget.reports.vm.ChartData
import aktual.budget.reports.vm.dashboard.DashboardItem
import aktual.budget.reports.vm.dashboard.isRenamable
import aktual.budget.reports.vm.dashboard.name
import aktual.core.icons.material.Delete
import aktual.core.icons.material.Edit
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualAlertDialogContent
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.SwipeAction
import aktual.core.ui.SwipeToReveal
import aktual.core.ui.contrastingTextColor
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun DashboardItem(
  item: DashboardItem,
  observer: DashboardItemObserver,
  isOpen: Boolean,
  onOpenChange: (Boolean) -> Unit,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
) {
  val flow = remember(item) { observer(item) }
  val chartData by flow.collectAsStateWithLifecycle(initialValue = null)

  DashboardItem(
    item = item,
    chartData = chartData,
    isOpen = isOpen,
    onOpenChange = onOpenChange,
    onAction = onAction,
    modifier = modifier,
  )
}

@Composable
private fun DashboardItem(
  item: DashboardItem,
  chartData: ChartData?,
  isOpen: Boolean,
  onOpenChange: (Boolean) -> Unit,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
) {
  var showRenameDialog by remember { mutableStateOf(false) }
  var showDeleteDialog by remember { mutableStateOf(false) }

  val renameAction =
    SwipeAction(
      text = Strings.reportsDashboardRename,
      icon = MaterialIcons.Edit,
      background = lerp(colors.tableBackground, Black, fraction = 0.1f),
      foreground = colors.tableText,
      onClick = { showRenameDialog = true },
    )
  val deleteAction =
    SwipeAction(
      text = Strings.reportsDashboardDelete,
      icon = MaterialIcons.Delete,
      background = colors.errorText,
      foreground = colors.errorText.contrastingTextColor(),
      onClick = { showDeleteDialog = true },
    )

  // Swiping still works while loading, so a chart that never loads can be deleted
  SwipeToReveal(
    actions =
      if (item.isRenamable) {
        persistentListOf(renameAction, deleteAction)
      } else {
        persistentListOf(deleteAction)
      },
    isOpen = isOpen,
    onOpenChange = onOpenChange,
    modifier = modifier,
  ) {
    ReportCard(
      chartData = chartData,
      onClick = {
        when {
          isOpen -> onOpenChange(false)
          chartData != null -> onAction(Action.OpenItem(item.id))
        }
      },
      onAction = onAction,
    )
  }

  if (showRenameDialog) {
    NameDialog(
      title = Strings.reportsDashboardRenameReport,
      placeholder = Strings.reportsDashboardReportName,
      confirmText = Strings.reportsDashboardNameSave,
      initialName = item.name.orEmpty(),
      onConfirm = { name ->
        showRenameDialog = false
        onOpenChange(false)
        onAction(Action.Rename(item, name))
      },
      onDismiss = { showRenameDialog = false },
    )
  }

  if (showDeleteDialog) {
    AktualAlertDialog(onDismissRequest = { showDeleteDialog = false }) {
      DeleteReportDialogContent(
        name = item.name,
        onConfirm = {
          showDeleteDialog = false
          onOpenChange(false)
          onAction(Action.Delete(item.id))
        },
        onDismiss = { showDeleteDialog = false },
      )
    }
  }
}

@Composable
private fun ReportCard(
  chartData: ChartData?,
  onClick: () -> Unit,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
) =
  Box(
    modifier =
      modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .background(colors.tableBackground, CardShape)
        .clickable(onClick = onClick),
  ) {
    if (chartData != null) {
      ReportChart(
        modifier = Modifier.fillMaxWidth().padding(ChartPadding).height(ChartHeight),
        data = chartData,
        compact = true,
        onAction = onAction,
      )
    } else {
      LoadingChart(modifier = Modifier.fillMaxWidth().height(ChartHeight + ChartPadding * 2))
    }
  }

private val ChartHeight = 200.dp
private val ChartPadding = 8.dp

@Composable
internal fun ShimmerDashboardItem(modifier: Modifier = Modifier) =
  LoadingChart(modifier = modifier.fillMaxWidth().height(ChartHeight + ChartPadding * 2))

@Composable
private fun LoadingChart(modifier: Modifier = Modifier) {
  val shimmer = rememberShimmer(Window)
  Box(modifier = modifier.clip(CardShape).shimmer(shimmer).background(colors.tableText))
}

@Composable
private fun DeleteReportDialogContent(name: String?, onConfirm: () -> Unit, onDismiss: () -> Unit) =
  AktualAlertDialogContent(
    title =
      if (name.isNullOrBlank()) {
        Strings.reportsDashboardDeleteReportUntitled
      } else {
        Strings.reportsDashboardDeleteReportTitle(name)
      },
    highlight = colors.errorText,
    buttons = {
      TextButton(onClick = onDismiss) { Text(Strings.reportsDashboardDeleteReportCancel) }
      TextButton(onClick = onConfirm) {
        Text(Strings.reportsDashboardDeleteReportConfirm, color = colors.errorText)
      }
    },
    content = {},
  )

@Preview
@Composable
private fun PreviewReportDashboardItem(
  @PreviewParameter(ReportDashboardItemProvider::class) params: ColoredParams<DashboardItemParams>,
) =
  PreviewWithColoredParams(params) {
    DashboardItem(
      item = item,
      chartData = chartData,
      isOpen = false,
      onOpenChange = {},
      onAction = {},
    )
  }

@Preview
@Composable
private fun PreviewDeleteReportDialog(
  @PreviewParameter(DeleteReportDialogProvider::class) params: ColoredParams<String?>,
) =
  PreviewWithColoredParams(params) {
    DeleteReportDialogContent(name = params.data, onConfirm = {}, onDismiss = {})
  }

private class DeleteReportDialogProvider : ColoredParameterProvider<String?>("Net worth", null)

private data class DashboardItemParams(val item: DashboardItem, val chartData: ChartData?)

private class ReportDashboardItemProvider :
  ColoredParameterProvider<DashboardItemParams>(
    DashboardItemParams(PREVIEW_DASHBOARD_ITEM_1, PREVIEW_CASH_FLOW_DATA),
    DashboardItemParams(PREVIEW_DASHBOARD_ITEM_2, PREVIEW_NET_WORTH_DATA),
    DashboardItemParams(PREVIEW_DASHBOARD_ITEM_3, PER_TRANSACTION_DATA),
    DashboardItemParams(PREVIEW_DASHBOARD_ITEM_3, chartData = null),
  )
