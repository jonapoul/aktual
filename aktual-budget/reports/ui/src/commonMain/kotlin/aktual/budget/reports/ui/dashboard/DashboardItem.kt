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
import aktual.core.ui.contrastingTextColor
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import kotlin.math.roundToInt

// Resting positions of a card: closed, or swiped left to reveal its action buttons
private enum class SwipeState {
  Closed,
  Open,
}

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

  val numButtons = if (item.isRenamable) 2 else 1
  val openOffsetPx = with(LocalDensity.current) { (SwipeButtonWidth * numButtons).toPx() }
  val swipeState =
    remember(openOffsetPx) {
      AnchoredDraggableState(initialValue = SwipeState.Closed).apply {
        updateAnchors(
          DraggableAnchors {
            SwipeState.Closed at 0f
            SwipeState.Open at -openOffsetPx
          }
        )
      }
    }

  // Tell the parent when this card settles, so it can keep only one card open
  val currentOnOpenChange by rememberUpdatedState(onOpenChange)
  LaunchedEffect(swipeState) {
    snapshotFlow { swipeState.settledValue }
      .collect { settled -> currentOnOpenChange(settled == Open) }
  }

  // Claim the open slot as soon as a drag starts, so any other open card closes straight away
  val interactionSource = remember { MutableInteractionSource() }
  LaunchedEffect(interactionSource) {
    interactionSource.interactions.collect { interaction ->
      if (interaction is DragInteraction.Start) currentOnOpenChange(true)
    }
  }

  LaunchedEffect(isOpen) {
    if (!isOpen && swipeState.currentValue != Closed) {
      swipeState.animateTo(Closed)
    }
  }

  val revealedPx = { swipeState.offset.let { x -> if (x.isNaN()) 0f else -x } }

  Box(modifier = modifier.fillMaxWidth().clip(CardShape)) {
    Row(modifier = Modifier.matchParentSize(), horizontalArrangement = Arrangement.End) {
      if (item.isRenamable) {
        SwipeButton(
          text = Strings.reportsDashboardRename,
          icon = MaterialIcons.Edit,
          background = lerp(colors.tableBackground, Black, fraction = 0.1f),
          foreground = colors.tableText,
          revealedPx = revealedPx,
          indexFromEnd = 1,
          onClick = { showRenameDialog = true },
        )
      }

      SwipeButton(
        text = Strings.reportsDashboardDelete,
        icon = MaterialIcons.Delete,
        background = colors.errorText,
        foreground = colors.errorText.contrastingTextColor(),
        revealedPx = revealedPx,
        indexFromEnd = 0,
        onClick = { showDeleteDialog = true },
      )
    }

    // Swiping still works while loading, so a chart that never loads can be deleted
    ReportCard(
      chartData = chartData,
      onClick = {
        when {
          isOpen -> onOpenChange(false)
          chartData != null -> onAction(Action.OpenItem(item.id))
        }
      },
      onAction = onAction,
      modifier =
        Modifier.offset {
            val x = swipeState.offset
            IntOffset(x = if (x.isNaN()) 0 else x.roundToInt(), y = 0)
          }
          .anchoredDraggable(
            state = swipeState,
            orientation = Horizontal,
            interactionSource = interactionSource,
          ),
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
        .clickable(onClick = onClick)
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

@Composable
private fun SwipeButton(
  text: String,
  icon: ImageVector,
  background: Color,
  foreground: Color,
  revealedPx: () -> Float,
  indexFromEnd: Int,
  onClick: () -> Unit,
) {
  val widthPx = with(LocalDensity.current) { SwipeButtonWidth.toPx() }

  // 0 while hidden behind the card, 1 once fully uncovered. Buttons nearer the end uncover first
  fun progress() = ((revealedPx() - indexFromEnd * widthPx) / widthPx).coerceIn(0f, 1f)

  Column(
    modifier =
      Modifier.fillMaxHeight()
        .width(SwipeButtonWidth)
        .background(background)
        .clickable(onClick = onClick),
    horizontalAlignment = CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    // Pops in with a slight overshoot and a twist as the card slides away
    Icon(
      modifier =
        Modifier.graphicsLayer {
          val progress = progress()
          val scale = EaseOutBack.transform(progress)
          alpha = progress
          scaleX = scale
          scaleY = scale
          rotationZ = (1f - progress) * -ICON_TWIST_DEGREES
        },
      imageVector = icon,
      contentDescription = null,
      tint = foreground,
    )

    Text(
      modifier =
        Modifier.graphicsLayer {
          val progress = progress()
          alpha = progress
          translationY = (1f - progress) * LabelRise.toPx()
        },
      text = text,
      color = foreground,
    )
  }
}

private const val ICON_TWIST_DEGREES = 90f
private val LabelRise = 8.dp

private val ChartHeight = 200.dp
private val ChartPadding = 8.dp
private val SwipeButtonWidth = 80.dp

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
    titleColor = colors.errorText,
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
  @PreviewParameter(ReportDashboardItemProvider::class) params: ColoredParams<DashboardItemParams>
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
  @PreviewParameter(DeleteReportDialogProvider::class) params: ColoredParams<String?>
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
