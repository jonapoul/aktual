package aktual.budget.reports.ui.report

import aktual.budget.model.WidgetId
import aktual.budget.model.WidgetType
import aktual.budget.reports.ui.Tags
import aktual.budget.reports.ui.charts.FormulaChart
import aktual.budget.reports.ui.charts.PREVIEW_FORMULA_DATA
import aktual.budget.reports.ui.charts.resolveColor
import aktual.budget.reports.ui.dashboard.NameDialog
import aktual.budget.reports.ui.string
import aktual.budget.reports.vm.FormulaData
import aktual.budget.reports.vm.FormulaEdits
import aktual.budget.reports.vm.FormulaQuery
import aktual.budget.reports.vm.QueryEditorData
import aktual.budget.reports.vm.report.FormulaReportState
import aktual.budget.reports.vm.report.FormulaReportViewModel
import aktual.budget.reports.vm.toEdits
import aktual.budget.reports.vm.withNewQuery
import aktual.budget.reports.vm.withQuery
import aktual.budget.reports.vm.withQueryRenamed
import aktual.budget.reports.vm.withoutQuery
import aktual.core.icons.Add
import aktual.core.icons.AktualIcons
import aktual.core.icons.Delete
import aktual.core.icons.Pencil1
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
import aktual.core.ui.HazedTopBarState
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.PrimaryIconButton
import aktual.core.ui.PrimaryTextButton
import aktual.core.ui.TonalButton
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.switch
import aktual.core.ui.transparentTopAppBarColors
import aktual.core.ui.verticalScrollWithBar
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet

// Views a formula card, and edits it in place. Everything that differs between the two animates
@Composable
@Suppress("CyclomaticComplexMethod")
internal fun FormulaReportScreen(
  id: WidgetId,
  back: BackNavigator,
  viewModel: FormulaReportViewModel = formulaReportViewModel(id),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val queryEditorData by viewModel.queryEditorData.collectAsStateWithLifecycle()
  var page by rememberSaveable { mutableStateOf<FormulaField?>(null) }
  var queryName by rememberSaveable { mutableStateOf<String?>(null) }
  var dialog by rememberSaveable { mutableStateOf<FormulaDialog?>(null) }
  val loaded = state as? Loaded
  val isEditing = loaded?.isEditing == true

  val stopEditing = {
    if (viewModel.hasUnsavedChanges()) dialog = Discard else viewModel.discard()
  }

  val field = page
  val openQuery = queryName?.let { name -> loaded?.draft?.queries?.get(name)?.let { name to it } }
  if (field != null && loaded != null && isEditing) {
    FormulaFieldScreen(
      field = field,
      initialText = loaded.draft.text(field),
      preview = loaded.preview,
      queryNames = loaded.draft.queries.keys.toImmutableList(),
      onChange = { text -> viewModel.edit { it.withText(field, text) } },
      onClose = { page = null },
    )
  } else if (openQuery != null && isEditing) {
    val (name, query) = openQuery
    QueryScreen(
      name = name,
      query = query,
      data = queryEditorData,
      isNameFree = { it !in loaded.draft.queries },
      onChange = { changed ->
        viewModel.edit { it.withQuery(name, changed) }
      },
      onRename = { newName ->
        viewModel.edit { it.withQueryRenamed(name, newName) }
        queryName = newName
      },
      onDelete = {
        viewModel.edit { it.withoutQuery(name) }
        queryName = null
      },
      onClose = { queryName = null },
    )
  } else {
    BackHandler(enabled = isEditing) { stopEditing() }

    FormulaReportScaffold(
      state = state,
      queryEditorData = queryEditorData,
      onAction = { action ->
        when (action) {
          Back -> back()
          StartEditing -> viewModel.startEditing()
          StopEditing -> stopEditing()
          Save -> viewModel.save()
          is Edit -> viewModel.edit(action.transform)
          EditName -> dialog = Name
          EditFormula -> page = Formula
          EditColorRule -> page = ColorRule
          EditFontSize -> dialog = FontSize
          is EditQuery -> queryName = action.name
          AddQuery -> dialog = NewQuery
        }
      },
    )
  }

  FormulaDialogs(
    dialog = dialog,
    loaded = loaded,
    onDiscard = viewModel::discard,
    onEdit = viewModel::edit,
    onAddQuery = { name ->
      queryEditorData?.let { data ->
        viewModel.edit { it.withNewQuery(name, data.currentMonth) }
        queryName = name
      }
    },
    onDismiss = { dialog = null },
  )
}

@Composable
private fun FormulaDialogs(
  dialog: FormulaDialog?,
  loaded: FormulaReportState.Loaded?,
  onDiscard: () -> Unit,
  onEdit: ((FormulaEdits) -> FormulaEdits) -> Unit,
  onAddQuery: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  when (dialog) {
    Discard -> {
      DiscardChangesDialog(
        message = Strings.reportsFormulaEditorDiscardMessage,
        onDiscard = {
          onDismiss()
          onDiscard()
        },
        onCancel = onDismiss,
      )
    }
    Name -> {
      NameDialog(
        title = Strings.reportsDashboardRenameReport,
        placeholder = Strings.reportsDashboardReportName,
        confirmText = Strings.reportsDashboardNameSave,
        initialName = loaded?.draft?.name.orEmpty(),
        onConfirm = { name ->
          onEdit { it.copy(name = name) }
          onDismiss()
        },
        onDismiss = onDismiss,
      )
    }
    FontSize -> {
      if (loaded != null) {
        FontSizeDialog(
          initialMode = loaded.draft.fontSizeMode,
          initialSize = loaded.draft.staticFontSize,
          preview = loaded.preview,
          onApply = { mode, size ->
            onEdit { it.copy(fontSizeMode = mode, staticFontSize = size) }
            onDismiss()
          },
          onDismiss = onDismiss,
        )
      }
    }
    NewQuery -> {
      NewQueryDialog(
        queries = loaded?.draft?.queries.orEmpty().keys.toImmutableSet(),
        onConfirm = { name ->
          onDismiss()
          onAddQuery(name)
        },
        onDismiss = onDismiss,
      )
    }
    null -> {}
  }
}

@Composable
internal fun NewQueryDialog(
  queries: ImmutableSet<String>,
  onConfirm: (String) -> Unit,
  onDismiss: () -> Unit,
) =
  NameDialog(
    title = Strings.reportsFormulaQueryNew,
    placeholder = Strings.reportsFormulaQueryName,
    confirmText = Strings.reportsFormulaQueryCreate,
    capitalization = None,
    isValid = { it !in queries },
    onDismiss = onDismiss,
    onConfirm = onConfirm,
  )

private enum class FormulaDialog {
  Discard,
  Name,
  FontSize,
  NewQuery,
}

private fun FormulaEdits.text(field: FormulaField): String =
  when (field) {
    Formula -> formula
    ColorRule -> colorFormula
  }

private fun FormulaEdits.withText(field: FormulaField, text: String): FormulaEdits =
  when (field) {
    Formula -> copy(formula = text)
    ColorRule -> copy(colorFormula = text)
  }

@Composable
private fun formulaReportViewModel(id: WidgetId) =
  assistedMetroViewModel<FormulaReportViewModel, FormulaReportViewModel.Factory>(key = id.value) {
    create(id)
  }

@Composable
internal fun FormulaReportScaffold(
  state: FormulaReportState,
  onAction: (FormulaReportAction) -> Unit,
  modifier: Modifier = Modifier,
  queryEditorData: QueryEditorData? = null,
) {
  val hazeState = rememberHazedTopBarState()
  val scrollState = rememberScrollState()
  val loaded = state as? Loaded

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      FormulaReportTopBar(
        loaded = loaded,
        hazeState = hazeState,
        scrollState = scrollState,
        onAction = onAction,
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
            ResultCard(preview = state.preview, isEditing = state.isEditing)
            Settings(
              draft = state.draft,
              preview = state.preview,
              names = queryEditorData?.names,
              isEditing = state.isEditing,
              onAction = onAction,
            )
            BottomSpacing()
          }
        }
      }
    }
  }
}

@Composable
private fun FormulaReportTopBar(
  loaded: FormulaReportState.Loaded?,
  hazeState: HazedTopBarState,
  scrollState: ScrollState,
  onAction: (FormulaReportAction) -> Unit,
) {
  val isEditing = loaded?.isEditing == true
  TopAppBar(
    modifier = Modifier.hazedTopBar(hazeState, scrollOffset = { scrollState.value.toFloat() }),
    colors = colors.transparentTopAppBarColors(),
    navigationIcon = {
      AnimatedContent(targetState = isEditing, transitionSpec = { swap() }) { editing ->
        if (editing) {
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
      }
    },
    title = {
      val name = loaded?.draft?.name?.takeUnless { it.isBlank() }
      val title =
        when {
          isEditing -> Strings.reportsFormulaViewerEdit
          name != null -> name
          else -> WidgetType.Formula.string()
        }
      AnimatedContent(
        targetState = title,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
      ) {
        Text(text = it)
      }
    },
    actions = {
      AnimatedContent(targetState = isEditing, transitionSpec = { swap() }) { editing ->
        if (editing) {
          PrimaryTextButton(
            modifier = Modifier.padding(end = 8.dp).testTag(Tags.SaveReport),
            text = Strings.reportsFormulaEditorSave,
            shape = CircleShape,
            isEnabled = loaded?.hasChanges == true,
            onClick = { onAction(Save) },
          )
        } else if (loaded != null) {
          PrimaryIconButton(
            modifier = Modifier.padding(horizontal = 8.dp).testTag(Tags.EditReport),
            imageVector = AktualIcons.Pencil1,
            contentDescription = Strings.reportsFormulaViewerEdit,
            size = 20.dp,
            shape = CircleShape,
            onClick = { onAction(StartEditing) },
          )
        }
      }
    },
  )
}

private fun <S> AnimatedContentTransitionScope<S>.swap(): ContentTransform =
  fadeIn() + scaleIn(initialScale = SWAP_SCALE) togetherWith
    fadeOut() + scaleOut(targetScale = SWAP_SCALE)

// While editing, a dashed outline and a label mark the card as a preview of the draft
@Composable
private fun ResultCard(preview: FormulaData?, isEditing: Boolean) {
  val border = colors.tableBorder
  val outlineAlpha by animateFloatAsState(if (isEditing) 1f else 0f)
  Box(
    modifier =
      Modifier.fillMaxWidth()
        .height(RESULT_HEIGHT)
        .background(colors.tableBackground, CardShape)
        .drawBehind {
          val outline = CardShape.createOutline(size, layoutDirection, this)
          drawOutline(
            outline = outline,
            color = border,
            alpha = outlineAlpha,
            style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(DASHES)),
          )
        }
        .padding(8.dp),
  ) {
    if (preview == null) {
      CircularProgressIndicator(modifier = Modifier.align(Center), color = colors.pageText)
    } else {
      FormulaChart(modifier = Modifier.fillMaxSize(), data = preview, compact = false)
    }
    AnimatedVisibility(visible = isEditing, enter = fadeIn(), exit = fadeOut()) {
      Text(
        modifier = Modifier.padding(4.dp),
        text = Strings.reportsFormulaEditorPreview.uppercase(),
        style = typography.labelMedium.copy(fontWeight = SemiBold, letterSpacing = 0.08.em),
        color = colors.tableHeaderText,
      )
    }
  }
}

@Composable
private fun Settings(
  draft: FormulaEdits,
  preview: FormulaData?,
  names: ImmutableMap<String, String>?,
  isEditing: Boolean,
  onAction: (FormulaReportAction) -> Unit,
) {
  // Rows only open their editors while editing
  fun open(action: FormulaReportAction): (() -> Unit)? =
    if (isEditing) {
      { onAction(action) }
    } else {
      null
    }

  Section(
    label =
      if (isEditing) Strings.reportsFormulaEditorReport else Strings.reportsFormulaEditorFormula,
  ) {
    AnimatedVisibility(
      visible = isEditing,
      enter = fadeIn() + expandVertically(),
      exit = fadeOut() + shrinkVertically(),
    ) {
      Column {
        ValueRow(label = Strings.reportsFormulaEditorName, onClick = open(EditName)) {
          Value(
            text =
              draft.name.takeUnless { it.isNullOrBlank() } ?: Strings.reportsFormulaViewerNotSet,
          )
        }
        Divider()
      }
    }
    SettingRow(onClick = open(EditFormula)) {
      AnimatedVisibility(
        visible = isEditing,
        enter = fadeIn() + expandHorizontally(),
        exit = fadeOut() + shrinkHorizontally(),
      ) {
        Text(
          text = Strings.reportsFormulaEditorFormula,
          style = typography.bodyLarge,
          color = colors.tableText,
        )
      }
      FormulaText(modifier = Modifier.weight(1f), formula = draft.formula)
    }
  }

  Section(label = Strings.reportsFormulaViewerAppearance) {
    ValueRow(label = Strings.reportsFormulaViewerColorRule, onClick = open(EditColorRule)) {
      val color = preview?.resolveColor()
      if (color != null) {
        Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
      }
      Value(
        text = draft.colorFormula.ifBlank { Strings.reportsFormulaViewerNotSet },
        monospace = draft.colorFormula.isNotBlank(),
      )
    }
    Divider()
    ValueRow(label = Strings.reportsFormulaEditorFontSize, onClick = open(EditFontSize)) {
      Value(text = fontSizeString(draft.fontSizeMode, draft.staticFontSize))
    }
    Divider()
    ShowTitleRow(showTitle = draft.showTitle, isEditing = isEditing, onAction = onAction)
  }

  // The add button waits for the editor data, which a new query takes its month from
  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    Section(label = Strings.reportsFormulaViewerQueries(draft.queries.size)) {
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
        QueryRow(
          name = name,
          query = query,
          names = names,
          isEditing = isEditing,
          onClick = open(FormulaReportAction.EditQuery(name)),
        )
      }
    }
    AnimatedVisibility(
      visible = isEditing,
      enter = fadeIn() + expandVertically(),
      exit = fadeOut() + shrinkVertically(),
    ) {
      TonalButton(
        modifier = Modifier.fillMaxWidth().testTag(Tags.AddQuery),
        icon = AktualIcons.Add,
        text = Strings.reportsFormulaQueryAdd,
        isEnabled = names != null,
        onClick = { onAction(AddQuery) },
      )
    }
  }
}

@Composable
private fun FormulaText(formula: String, modifier: Modifier = Modifier) =
  if (formula.isBlank()) {
    Text(
      modifier = modifier,
      text = Strings.reportsFormulaViewerNotSet,
      style = typography.bodyMedium,
      color = colors.tableHeaderText,
    )
  } else {
    Text(
      modifier = modifier,
      text = formula,
      style = typography.bodyMedium.copy(fontFamily = Monospace),
      color = colors.pageTextPositive,
      maxLines = 1,
      overflow = Ellipsis,
    )
  }

// A switch while editing, and On or Off while viewing
@Composable
private fun ShowTitleRow(
  showTitle: Boolean,
  isEditing: Boolean,
  onAction: (FormulaReportAction) -> Unit,
) {
  val toggle = { show: Boolean -> onAction(FormulaReportAction.Edit { it.copy(showTitle = show) }) }
  val onOff = if (showTitle) Strings.reportsFormulaViewerOn else Strings.reportsFormulaViewerOff
  SettingRow(
    modifier = Modifier.testTag(Tags.ShowTitle),
    onClick = { toggle(!showTitle) }.takeIf { isEditing },
    showChevron = false,
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = Strings.reportsFormulaEditorShowTitle,
      style = typography.bodyLarge,
      color = colors.tableText,
    )
    AnimatedContent(
      targetState = isEditing,
      transitionSpec = { fadeIn() togetherWith fadeOut() },
      contentAlignment = CenterEnd,
    ) { editing ->
      if (editing) {
        Switch(checked = showTitle, onCheckedChange = toggle, colors = colors.switch())
      } else {
        Value(text = onOff)
      }
    }
  }
}

// The full summary while viewing, shrinking to a condition count while editing
@Composable
private fun QueryRow(
  name: String,
  query: FormulaQuery,
  names: ImmutableMap<String, String>?,
  isEditing: Boolean,
  onClick: (() -> Unit)?,
) =
  SettingRow(modifier = Modifier.testTag(Tags.QueryRow), onClick = onClick) {
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text(
        text = name,
        style = typography.bodyLarge.copy(fontFamily = Monospace, fontWeight = Medium),
        color = colors.tableText,
      )
      AnimatedVisibility(
        visible = !isEditing,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          // Entity names load separately, so the summary waits for them rather than showing ids
          if (names != null) {
            QueryDetail(text = query.conditionsSummary(names))
          }
          QueryDetail(text = query.timeFrame.label())
        }
      }
    }
    AnimatedVisibility(
      visible = isEditing,
      enter = fadeIn() + expandHorizontally(),
      exit = fadeOut() + shrinkHorizontally(),
    ) {
      Value(
        text = Plurals.reportsFormulaEditorConditions(query.conditions.size, query.conditions.size),
      )
    }
  }

@Composable
private fun QueryDetail(text: String) =
  Text(text = text, style = typography.bodySmall, color = colors.tableHeaderText)

private const val SWAP_SCALE = 0.8f
private val CONTENT_PADDING = 8.dp
private val RESULT_HEIGHT = 160.dp
private val DASHES = floatArrayOf(12f, 8f)

@PortraitPreview
@Composable
private fun PreviewFormulaReportScaffold(
  @PreviewParameter(FormulaReportProvider::class) params: ColoredParams<FormulaReportState>,
) =
  PreviewWithColoredParams(params) {
    FormulaReportScaffold(state = this, queryEditorData = PREVIEW_QUERY_DATA, onAction = {})
  }

private class FormulaReportProvider :
  ColoredParameterProvider<FormulaReportState>(
    FormulaReportState.Loaded(
      draft = PREVIEW_FORMULA_META.toEdits(),
      preview = PREVIEW_FORMULA_DATA,
      isEditing = false,
      hasChanges = false,
    ),
    FormulaReportState.Loaded(
      draft = PREVIEW_FORMULA_META.toEdits(),
      preview = PREVIEW_FORMULA_DATA,
      isEditing = true,
      hasChanges = true,
    ),
    Loading,
    NotFound,
  )
