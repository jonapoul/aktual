package aktual.budget.reports.ui.report

import aktual.budget.model.WidgetId
import aktual.budget.reports.ui.Tags
import aktual.budget.reports.ui.charts.FormulaChart
import aktual.budget.reports.ui.charts.PREVIEW_FORMULA_DATA
import aktual.budget.reports.vm.FormulaEdits
import aktual.budget.reports.vm.FormulaQuery
import aktual.budget.reports.vm.report.EditFormulaReportState
import aktual.budget.reports.vm.report.EditFormulaReportViewModel
import aktual.budget.reports.vm.toEdits
import aktual.core.icons.material.Clear
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BackHandler
import aktual.core.ui.BottomSpacing
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.PrimaryTextButton
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.switch
import aktual.core.ui.transparentTopAppBarColors
import aktual.core.ui.verticalScrollWithBar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily.Companion.Monospace
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

@Composable
fun EditFormulaReportScreen(
  id: WidgetId,
  back: BackNavigator,
  viewModel: EditFormulaReportViewModel = editFormulaReportViewModel(id),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  var showDiscardDialog by remember { mutableStateOf(false) }
  val hasChanges = (state as? EditFormulaReportState.Loaded)?.hasChanges == true

  BackHandler(enabled = hasChanges) { showDiscardDialog = true }

  EditFormulaReportScaffold(
    state = state,
    onAction = { action ->
      when (action) {
        Close -> {
          if (viewModel.hasUnsavedChanges()) showDiscardDialog = true else back()
        }
        Save -> {
          viewModel.save()
          back()
        }
        is Edit -> {
          viewModel.edit(action.transform)
        }
      }
    },
  )

  if (showDiscardDialog) {
    DiscardChangesDialog(
      message = Strings.reportsFormulaEditorDiscardMessage,
      onDiscard = {
        showDiscardDialog = false
        back()
      },
      onCancel = { showDiscardDialog = false },
    )
  }
}

@Composable
private fun editFormulaReportViewModel(id: WidgetId) =
  assistedMetroViewModel<EditFormulaReportViewModel, EditFormulaReportViewModel.Factory>(
    key = id.value,
  ) {
    create(id)
  }

@Composable
internal fun EditFormulaReportScaffold(
  state: EditFormulaReportState,
  onAction: (EditFormulaAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  val hazeState = rememberHazedTopBarState()
  val scrollState = rememberScrollState()

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, scrollOffset = { scrollState.value.toFloat() }),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = {
          IconButton(modifier = Modifier.testTag(Tags.CloseEditor), onClick = { onAction(Close) }) {
            Icon(
              imageVector = MaterialIcons.Clear,
              contentDescription = Strings.reportsFormulaEditorClose,
            )
          }
        },
        title = { Text(text = Strings.reportsFormulaViewerEdit) },
        actions = {
          PrimaryTextButton(
            modifier = Modifier.padding(end = 8.dp).testTag(Tags.SaveReport),
            text = Strings.reportsFormulaEditorSave,
            shape = CircleShape,
            isEnabled = (state as? EditFormulaReportState.Loaded)?.hasChanges == true,
            onClick = { onAction(Save) },
          )
        },
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()
      val contentModifier = Modifier.hazedTopBarContent(hazeState, innerPadding).fillMaxSize()
      when (state) {
        Loading -> {
          Box(modifier = contentModifier, contentAlignment = Center) {
            CircularProgressIndicator(color = colors.pageText)
          }
        }
        NotFound -> {
          Box(modifier = contentModifier, contentAlignment = Center) {
            Text(text = Strings.reportsNotFound, color = colors.pageText)
          }
        }
        is Loaded -> {
          Column(
            modifier =
              contentModifier
                .verticalScrollWithBar(scrollState)
                .padding(hazedTopBarContentPadding(hazeState, innerPadding))
                .padding(CONTENT_PADDING),
            verticalArrangement = Arrangement.spacedBy(20.dp),
          ) {
            PreviewCard(state)
            Settings(draft = state.draft, onAction = onAction)
            BottomSpacing()
          }
        }
      }
    }
  }
}

@Composable
private fun PreviewCard(state: EditFormulaReportState.Loaded) {
  val border = colors.tableBorder
  Box(
    modifier =
      Modifier.fillMaxWidth()
        .height(PREVIEW_HEIGHT)
        .background(colors.tableBackground, CardShape)
        .drawBehind {
          val outline = CardShape.createOutline(size, layoutDirection, this)
          drawOutline(
            outline = outline,
            color = border,
            style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(DASHES)),
          )
        }
        .padding(8.dp),
  ) {
    Text(
      modifier = Modifier.padding(4.dp),
      text = Strings.reportsFormulaEditorPreview.uppercase(),
      style = typography.labelMedium.copy(fontWeight = SemiBold, letterSpacing = 0.08.em),
      color = colors.tableHeaderText,
    )
    val preview = state.preview
    if (preview == null) {
      CircularProgressIndicator(modifier = Modifier.align(Center), color = colors.pageText)
    } else {
      FormulaChart(modifier = Modifier.fillMaxSize(), data = preview, compact = false)
    }
  }
}

@Composable
private fun Settings(draft: FormulaEdits, onAction: (EditFormulaAction) -> Unit) {
  // Rows that open a sub-editor have no click handler until those screens exist
  Section(label = Strings.reportsFormulaEditorReport) {
    ValueRow(label = Strings.reportsFormulaEditorName, onClick = null) {
      Value(
        text = draft.name.takeUnless { it.isNullOrBlank() } ?: Strings.reportsFormulaViewerNotSet,
      )
    }
    Divider()
    ValueRow(label = Strings.reportsFormulaEditorFormula, onClick = null) {
      FormulaValue(draft.formula)
    }
  }

  Section(label = Strings.reportsFormulaViewerAppearance) {
    ValueRow(label = Strings.reportsFormulaViewerColorRule, onClick = null) {
      FormulaValue(draft.colorFormula)
    }
    Divider()
    ValueRow(label = Strings.reportsFormulaEditorFontSize, onClick = null) {
      Value(text = fontSizeString(draft.fontSizeMode, draft.staticFontSize))
    }
    Divider()
    val toggleTitle = { show: Boolean ->
      onAction(EditFormulaAction.Edit { it.copy(showTitle = show) })
    }
    SettingRow(
      modifier = Modifier.testTag(Tags.ShowTitle),
      onClick = { toggleTitle(!draft.showTitle) },
      showChevron = false,
    ) {
      Text(
        modifier = Modifier.weight(1f),
        text = Strings.reportsFormulaEditorShowTitle,
        style = typography.bodyLarge,
        color = colors.tableText,
      )
      Switch(checked = draft.showTitle, onCheckedChange = toggleTitle, colors = colors.switch())
    }
  }

  Section(label = Strings.reportsFormulaQueries) {
    if (draft.queries.isEmpty()) {
      SettingRow(onClick = null) {
        Text(
          modifier = Modifier.weight(1f),
          text = Strings.reportsFormulaQueriesEmpty,
          style = typography.bodyMedium,
          color = colors.tableHeaderText,
        )
      }
    }
    draft.queries.entries.forEachIndexed { index, (name, query) ->
      if (index > 0) Divider()
      QueryRow(name, query)
    }
  }
}

@Composable
private fun FormulaValue(formula: String) =
  if (formula.isBlank()) {
    Value(text = Strings.reportsFormulaViewerNotSet)
  } else {
    Text(
      text = formula,
      style = typography.bodySmall.copy(fontFamily = Monospace),
      color = colors.pageTextPositive,
      maxLines = 1,
      overflow = Ellipsis,
    )
  }

@Composable
private fun QueryRow(name: String, query: FormulaQuery) =
  SettingRow(onClick = null) {
    Text(
      modifier = Modifier.weight(1f),
      text = name,
      style = typography.bodyLarge.copy(fontFamily = Monospace, fontWeight = Medium),
      color = colors.tableText,
    )
    Value(
      text = Plurals.reportsFormulaEditorConditions(query.conditions.size, query.conditions.size),
    )
  }

private val CONTENT_PADDING = 8.dp
private val PREVIEW_HEIGHT = 160.dp
private val DASHES = floatArrayOf(12f, 8f)

@PortraitPreview
@Composable
private fun PreviewEditFormulaReportScaffold(
  @PreviewParameter(EditFormulaReportProvider::class) params: ColoredParams<EditFormulaReportState>,
) = PreviewWithColoredParams(params) { EditFormulaReportScaffold(state = this, onAction = {}) }

private class EditFormulaReportProvider :
  ColoredParameterProvider<EditFormulaReportState>(
    EditFormulaReportState.Loaded(
      draft = PREVIEW_FORMULA_META.toEdits(),
      preview = PREVIEW_FORMULA_DATA,
      hasChanges = true,
    ),
    Loading,
    NotFound,
  )
