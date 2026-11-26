package aktual.budget.reports.ui.report

import aktual.budget.reports.components.ChartAction
import aktual.budget.reports.components.ChartTags
import aktual.budget.reports.components.ChartZoomState
import aktual.budget.reports.components.ReportChart
import aktual.budget.reports.components.dashboardItem
import aktual.budget.reports.components.info
import aktual.budget.reports.ui.Tags
import aktual.budget.reports.vm.ChartData
import aktual.core.icons.AktualIcons
import aktual.core.icons.InformationOutline
import aktual.core.icons.Tuning
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.ZoomOut
import aktual.core.l10n.Strings
import aktual.core.ui.AktualIconButton
import aktual.core.ui.AktualProgressIndicator
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BottomSpacing
import aktual.core.ui.CardShape
import aktual.core.ui.Dimens
import aktual.core.ui.EditFabs
import aktual.core.ui.FabClearance
import aktual.core.ui.BackOrDiscardButton
import aktual.core.ui.OnAction
import aktual.core.ui.transparentTopAppBarColors
import alakazam.compose.VerticalSpacer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

// Changes to the draft show in the chart until saved or reverted
@Composable
internal fun ReportEditorTopBar(
  title: String,
  hasChanges: Boolean,
  canEdit: Boolean,
  preview: ChartData?,
  zoom: ChartZoomState,
  onShowInfo: () -> Unit,
  onAction: (ReportEditorAction) -> Unit,
  menu: @Composable () -> Unit = {},
) =
  TopAppBar(
    colors = colors.transparentTopAppBarColors(),
    navigationIcon = {
      BackOrDiscardButton(
        modifier = Modifier.testTag(Tags.DiscardReport),
        hasChanges = hasChanges,
        discardText = Strings.reportsTextDiscard,
        onBack = { onAction(Back) },
        onDiscard = { onAction(Back) },
      )
    },
    title = {
      Column {
        Text(text = title)
        AnimatedVisibility(visible = hasChanges) {
          Text(
            text = Strings.reportsCustomEditorUnsaved,
            style = typography.labelMedium,
            color = colors.warningText,
          )
        }
      }
    },
    actions = {
      if (zoom.isZoomed) {
        AktualIconButton(
          modifier = Modifier.testTag(ChartTags.ResetZoom),
          imageVector = MaterialIcons.ZoomOut,
          contentDescription = Strings.reportsResetZoom,
          onClick = zoom::reset,
        )
      }
      if (preview?.info()?.isNotEmpty() == true) {
        AktualIconButton(
          modifier = Modifier.testTag(ChartTags.ChartInfo),
          imageVector = AktualIcons.InformationOutline,
          contentDescription = Strings.reportsChartInfo,
          onClick = onShowInfo,
        )
      }
      if (canEdit) {
        AktualIconButton(
          modifier = Modifier.testTag(ChartTags.ChartSettings),
          imageVector = AktualIcons.Tuning,
          contentDescription = Strings.reportsChartSettings,
          onClick = { onAction(StartEditing) },
        )
      }
      menu()
    },
  )

// Revert keeps the screen open, unlike leaving with unsaved changes
@Composable
internal fun BoxScope.ReportEditorFabs(
  hasChanges: Boolean,
  canSave: Boolean,
  onAction: (ReportEditorAction) -> Unit,
) =
  Column(modifier = Modifier.align(BottomCenter)) {
    EditFabs(
      saveText = Strings.reportsFormulaEditorSave,
      discardText = Strings.reportsCustomEditorRevert,
      onSave = { onAction(Save) },
      onDiscard = { onAction(Revert) },
      isSaveVisible = canSave,
      isDiscardVisible = hasChanges,
      saveModifier = Modifier.testTag(Tags.SaveReport),
      discardModifier = Modifier.testTag(Tags.RevertReport),
    )
    BottomSpacing()
  }

@Composable
internal fun ReportEditorContent(
  preview: ChartData?,
  zoom: ChartZoomState,
  hasFabs: Boolean,
  modifier: Modifier = Modifier,
  onChartAction: OnAction<ChartAction> = OnAction<ChartAction> {},
  below: @Composable ColumnScope.() -> Unit = {},
) =
  Column(modifier = modifier.padding(Dimens.ScreenPadding)) {
    ChartCard(
      modifier = Modifier.fillMaxWidth().weight(1f),
      preview = preview,
      zoom = zoom,
      onAction = onChartAction,
    )
    below()
    FabSpace(isVisible = hasFabs)
    BottomSpacing()
  }

// Shrinks a chart that fills the screen while FABs show, so they don't cover it
@Composable
internal fun FabSpace(isVisible: Boolean) {
  val height by animateDpAsState(if (isVisible) FabClearance else 0.dp)
  VerticalSpacer(height)
}

@Composable
private fun ChartCard(
  preview: ChartData?,
  zoom: ChartZoomState,
  onAction: OnAction<ChartAction>,
  modifier: Modifier = Modifier,
) =
  Box(modifier = modifier.background(colors.tableBackground, CardShape).padding(8.dp)) {
    if (preview == null) {
      AktualProgressIndicator(Modifier.align(Center))
    } else {
      ReportChart(
        modifier = Modifier.fillMaxSize().dashboardItem(colors),
        data = preview,
        compact = false,
        onAction = onAction,
        zoom = zoom,
      )
    }
  }
