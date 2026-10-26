package aktual.budget.reports.ui.report

import aktual.budget.reports.ui.ActionListener
import aktual.budget.reports.ui.Tags
import aktual.budget.reports.ui.charts.ChartZoomState
import aktual.budget.reports.ui.charts.ReportChart
import aktual.budget.reports.ui.charts.info
import aktual.budget.reports.vm.ChartData
import aktual.core.icons.AktualIcons
import aktual.core.icons.Delete
import aktual.core.icons.InformationOutline
import aktual.core.icons.Pencil1
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.ZoomOut
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BareIconButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.CardShape
import aktual.core.ui.Dimens
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PrimaryIconButton
import aktual.core.ui.PrimaryTextButton
import aktual.core.ui.transparentTopAppBarColors
import aktual.core.ui.verticalScrollWithBar
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em

@Composable
internal fun ReportEditorTopBar(
  title: String,
  isEditing: Boolean,
  hasChanges: Boolean,
  canSave: Boolean,
  canEdit: Boolean,
  preview: ChartData?,
  zoom: ChartZoomState,
  onShowInfo: () -> Unit,
  onAction: (ReportEditorAction) -> Unit,
) =
  TopAppBar(
    colors = colors.transparentTopAppBarColors(),
    navigationIcon = {
      if (isEditing) {
        IconButton(
          modifier = Modifier.testTag(Tags.CloseEditor),
          onClick = { onAction(StopEditing) },
        ) {
          Icon(
            imageVector = AktualIcons.Delete,
            contentDescription = Strings.reportsFormulaEditorClose,
          )
        }
      } else {
        NavBackIconButton(onClick = { onAction(Back) })
      }
    },
    title = {
      Column {
        Text(text = if (isEditing) Strings.reportsFormulaViewerEdit else title)
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
      if (hasChanges) {
        TextButton(modifier = Modifier.testTag(Tags.RevertReport), onClick = { onAction(Revert) }) {
          Text(Strings.reportsCustomEditorRevert)
        }
      }
      if (canSave) {
        PrimaryTextButton(
          modifier = Modifier.padding(end = 8.dp).testTag(Tags.SaveReport),
          text = Strings.reportsFormulaEditorSave,
          shape = CircleShape,
          onClick = { onAction(Save) },
        )
      }
      if (!isEditing && zoom.range != null) {
        BareIconButton(
          modifier = Modifier.testTag(Tags.ResetZoom),
          imageVector = MaterialIcons.ZoomOut,
          contentDescription = Strings.reportsResetZoom,
          onClick = zoom::reset,
        )
      }
      if (!isEditing && preview?.info()?.isNotEmpty() == true) {
        BareIconButton(
          modifier = Modifier.testTag(Tags.ChartInfo),
          imageVector = AktualIcons.InformationOutline,
          contentDescription = Strings.reportsChartInfo,
          onClick = onShowInfo,
        )
      }
      if (!isEditing && canEdit) {
        PrimaryIconButton(
          modifier = Modifier.padding(horizontal = 8.dp).testTag(Tags.EditReport),
          imageVector = AktualIcons.Pencil1,
          contentDescription = Strings.reportsFormulaViewerEdit,
          size = 20.dp,
          shape = CircleShape,
          onClick = { onAction(StartEditing) },
        )
      }
    },
  )

// While editing, the chart shrinks to a preview above the settings, which scroll beneath it
@Composable
internal fun ReportEditorContent(
  preview: ChartData?,
  isEditing: Boolean,
  zoom: ChartZoomState,
  modifier: Modifier = Modifier,
  onChartAction: ActionListener = ActionListener {},
  below: @Composable ColumnScope.() -> Unit,
) =
  Column(modifier = modifier.padding(Dimens.ScreenPadding)) {
    ChartCard(
      modifier =
        if (isEditing) {
          Modifier.fillMaxWidth().height(PREVIEW_HEIGHT)
        } else {
          Modifier.fillMaxWidth().weight(1f)
        },
      preview = preview,
      isEditing = isEditing,
      zoom = zoom,
      onAction = onChartAction,
    )
    below()
    BottomSpacing()
  }

// For the settings below the preview, filling the rest of the screen
@Composable
context(scope: ColumnScope)
internal fun Modifier.editorSettingsModifier(): Modifier =
  with(scope) {
    fillMaxWidth().weight(1f).verticalScrollWithBar(rememberScrollState()).padding(top = 20.dp)
  }

@Composable
private fun ChartCard(
  preview: ChartData?,
  isEditing: Boolean,
  zoom: ChartZoomState,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
) =
  Box(modifier = modifier.background(colors.tableBackground, CardShape).padding(8.dp)) {
    if (preview == null) {
      CircularProgressIndicator(modifier = Modifier.align(Center), color = colors.pageText)
    } else {
      ReportChart(
        modifier = Modifier.fillMaxSize(),
        data = preview,
        compact = isEditing,
        onAction = onAction,
        zoom = zoom,
      )
    }
    AnimatedContent(
      modifier = Modifier.align(TopEnd),
      targetState = isEditing,
      transitionSpec = { fadeIn() togetherWith fadeOut() },
    ) { editing ->
      if (editing) {
        Text(
          modifier = Modifier.padding(4.dp),
          text = Strings.reportsFormulaEditorPreview.uppercase(),
          style = typography.labelMedium.copy(fontWeight = SemiBold, letterSpacing = 0.08.em),
          color = colors.tableHeaderText,
        )
      }
    }
  }

private val PREVIEW_HEIGHT = 280.dp
