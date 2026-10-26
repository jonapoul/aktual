package aktual.budget.reports.ui.dashboard

import aktual.budget.model.CustomReportId
import aktual.budget.reports.ui.Action
import aktual.budget.reports.ui.ActionListener
import aktual.budget.reports.ui.charts.PER_TRANSACTION_DATA
import aktual.budget.reports.ui.charts.PREVIEW_CASH_FLOW_DATA
import aktual.budget.reports.ui.charts.PREVIEW_NET_WORTH_DATA
import aktual.budget.reports.ui.charts.ReportChart
import aktual.budget.reports.vm.ChartData
import aktual.budget.reports.vm.CustomReportMeta
import aktual.budget.reports.vm.UnsupportedData
import aktual.budget.reports.vm.dashboard.DashboardItem
import aktual.budget.reports.vm.dashboard.isRenamable
import aktual.budget.reports.vm.dashboard.name
import aktual.core.icons.AktualIcons
import aktual.core.icons.Pencil1
import aktual.core.icons.Pin
import aktual.core.icons.Trash
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualAlertDialogContent
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.CardShape
import aktual.core.ui.ColorParameters
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.PreviewWithColors
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
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun DashboardItem(
  item: DashboardItem,
  isPinned: Boolean,
  canPin: Boolean,
  observer: DashboardItemObserver,
  isOpen: Boolean,
  onOpenChange: (Boolean) -> Unit,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  customReportNames: ImmutableMap<CustomReportId, String> = persistentMapOf(),
) {
  val flow = remember(item) { observer(item) }
  val chartData by flow.collectAsStateWithLifecycle(initialValue = null)

  DashboardItem(
    item = item,
    chartData = chartData,
    isPinned = isPinned,
    canPin = canPin,
    isOpen = isOpen,
    onOpenChange = onOpenChange,
    onAction = onAction,
    modifier = modifier,
    customReportNames = customReportNames,
  )
}

@Composable
private fun DashboardItem(
  item: DashboardItem,
  chartData: ChartData?,
  isPinned: Boolean,
  canPin: Boolean,
  isOpen: Boolean,
  onOpenChange: (Boolean) -> Unit,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  customReportNames: ImmutableMap<CustomReportId, String> = persistentMapOf(),
) {
  var showRenameDialog by remember { mutableStateOf(false) }
  var showDeleteDialog by remember { mutableStateOf(false) }

  val renameAction =
    SwipeAction(
      text = Strings.reportsDashboardRename,
      icon = AktualIcons.Pencil1,
      background = lerp(colors.tableBackground, Black, fraction = 0.1f),
      foreground = colors.tableText,
      onClick = { showRenameDialog = true },
    )
  val deleteAction =
    SwipeAction(
      text = Strings.reportsDashboardDelete,
      icon = AktualIcons.Trash,
      background = colors.errorText,
      foreground = colors.errorText.contrastingTextColor(),
      onClick = { showDeleteDialog = true },
    )

  val pinAction =
    SwipeAction(
      text = if (isPinned) Strings.reportsDashboardUnpin else Strings.reportsDashboardPin,
      icon = AktualIcons.Pin,
      background = lerp(colors.tableBackground, Black, fraction = 0.2f),
      foreground = colors.tableText,
      onClick = {
        onOpenChange(false)
        onAction(if (isPinned) Action.Unpin(item.id) else Action.Pin(item.id))
      },
    )

  // Swiping still works while loading, so a chart that never loads can be deleted. The home page
  // only has room for a few reports, so pinning goes once it's full
  SwipeToReveal(
    actions = buildList {
        if (isPinned || canPin) add(pinAction)
        if (item.isRenamable) add(renameAction)
        add(deleteAction)
      }
        .toImmutableList(),
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
      error = { name ->
        val meta = item.meta as? CustomReportMeta
        val taken = meta != null && customReportNames.any { (id, n) -> n == name && id != meta.id }
        if (taken) Strings.reportsCustomEditorNameTaken else null
      },
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
        onDeleteReport =
          if (item.hasSavedReport(chartData)) {
            {
              showDeleteDialog = false
              onOpenChange(false)
              onAction(Action.DeleteSavedReport(item))
            }
          } else {
            null
          },
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

internal val ChartHeight = 200.dp
internal val ChartPadding = 8.dp

@Composable
internal fun ShimmerDashboardItem(modifier: Modifier = Modifier) =
  LoadingChart(modifier = modifier.fillMaxWidth().height(ChartHeight + ChartPadding * 2))

@Composable
private fun LoadingChart(modifier: Modifier = Modifier) {
  val shimmer = rememberShimmer(Window)
  Box(modifier = modifier.clip(CardShape).shimmer(shimmer).background(colors.tableText))
}

// A custom report widget shows a saved report, which can be deleted as well as taken off the
// dashboard. Not once it's already gone
private fun DashboardItem.hasSavedReport(chartData: ChartData?): Boolean =
  meta is CustomReportMeta && (chartData as? UnsupportedData)?.reason != MissingReport

// With [onDeleteReport], the confirm button only removes the widget, and another deletes the saved
// report
@Composable
private fun DeleteReportDialogContent(
  name: String?,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
  onDeleteReport: (() -> Unit)? = null,
) =
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
      if (onDeleteReport == null) {
        TextButton(onClick = onConfirm) {
          Text(Strings.reportsDashboardDeleteReportConfirm, color = colors.errorText)
        }
      } else {
        TextButton(onClick = onConfirm) { Text(Strings.reportsDashboardDeleteReportRemove) }
        TextButton(onClick = onDeleteReport) {
          Text(Strings.reportsDashboardDeleteReportSaved, color = colors.errorText)
        }
      }
    },
    content = {
      if (onDeleteReport != null) Text(Strings.reportsDashboardDeleteReportSavedMessage)
    },
  )

@Preview
@Composable
private fun PreviewReportDashboardItem(
  @PreviewParameter(ReportDashboardItemProvider::class) params: ColorParams<DashboardItemParams>,
) =
  PreviewWithColorParams(params) {
    DashboardItem(
      item = item,
      chartData = chartData,
      isPinned = false,
      canPin = true,
      isOpen = false,
      onOpenChange = {},
      onAction = {},
    )
  }

@Preview
@Composable
private fun PreviewDeleteReportDialog(
  @PreviewParameter(DeleteReportDialogProvider::class) params: ColorParams<String?>,
) =
  PreviewWithColorParams(params) {
    DeleteReportDialogContent(name = params.data, onConfirm = {}, onDismiss = {})
  }

@Preview
@Composable
private fun PreviewDeleteSavedReportDialog(
  @PreviewParameter(ColorParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    DeleteReportDialogContent(name = null, onConfirm = {}, onDismiss = {}, onDeleteReport = {})
  }

private class DeleteReportDialogProvider : ColorParamsProvider<String?>("Net worth", null)

private data class DashboardItemParams(val item: DashboardItem, val chartData: ChartData?)

private class ReportDashboardItemProvider :
  ColorParamsProvider<DashboardItemParams>(
    DashboardItemParams(PREVIEW_DASHBOARD_ITEM_1, PREVIEW_CASH_FLOW_DATA),
    DashboardItemParams(PREVIEW_DASHBOARD_ITEM_2, PREVIEW_NET_WORTH_DATA),
    DashboardItemParams(PREVIEW_DASHBOARD_ITEM_3, PER_TRANSACTION_DATA),
    DashboardItemParams(PREVIEW_DASHBOARD_ITEM_3, chartData = null),
  )
