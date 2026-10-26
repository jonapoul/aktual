package aktual.budget.reports.ui.report

import aktual.budget.formula.ErrorValue
import aktual.budget.reports.ui.Tags
import aktual.budget.reports.ui.charts.PREVIEW_FORMULA_DATA
import aktual.budget.reports.ui.charts.displayString
import aktual.budget.reports.ui.charts.message
import aktual.budget.reports.ui.charts.resolveColor
import aktual.budget.reports.vm.FormulaData
import aktual.budget.reports.vm.FormulaOutput
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BackHandler
import aktual.core.ui.CardShape
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.Dimens
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.PrimaryTextButton
import aktual.core.ui.keyboardFocusRequester
import aktual.core.ui.transparentTopAppBarColors
import aktual.core.ui.verticalScrollWithBar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily.Companion.Monospace
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

// Edits one of the card's formulas in the draft, so the preview follows the text as it's typed.
// Back puts the text back as it was when the screen opened
@Composable
internal fun FormulaFieldScreen(
  field: FormulaField,
  initialText: String,
  preview: FormulaData?,
  queryNames: ImmutableList<String>,
  onChange: (String) -> Unit,
  onClose: () -> Unit,
) {
  val initial = rememberSaveable { initialText }
  val text = rememberTextFieldState(initial)
  val currentOnChange by rememberUpdatedState(onChange)
  LaunchedEffect(text) { snapshotFlow { text.text.toString() }.collect { currentOnChange(it) } }

  val cancel = {
    onChange(initial)
    onClose()
  }
  BackHandler(onBack = cancel)

  FormulaFieldScaffold(
    field = field,
    text = text,
    preview = preview,
    queryNames = queryNames,
    onBack = cancel,
    onDone = onClose,
  )
}

@Composable
private fun FormulaFieldScaffold(
  field: FormulaField,
  text: TextFieldState,
  preview: FormulaData?,
  queryNames: ImmutableList<String>,
  onBack: () -> Unit,
  onDone: () -> Unit,
) =
  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton(onClick = onBack) },
        title = { Text(text = field.title()) },
        actions = {
          PrimaryTextButton(
            modifier = Modifier.padding(end = 8.dp).testTag(Tags.FormulaDone),
            text = Strings.reportsFormulaEditorDone,
            shape = CircleShape,
            onClick = onDone,
          )
        },
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()
      Column(
        modifier =
          Modifier.fillMaxSize()
            .padding(innerPadding)
            .imePadding()
            .verticalScrollWithBar(rememberScrollState())
            .padding(Dimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        AktualTextField(
          modifier =
            Modifier.fillMaxWidth()
              .heightIn(min = field.minHeight())
              .focusRequester(keyboardFocusRequester())
              .testTag(Tags.FormulaField),
          state = text,
          placeholderText =
            if (field == Formula) Strings.reportsFormulaEditorFormulaPlaceholder else null,
          textStyle = typography.bodyMedium.copy(fontFamily = Monospace),
          keyboardOptions = KeyboardOptions(capitalization = None, autoCorrectEnabled = false),
        )

        if (field == ColorRule) {
          Text(
            modifier = Modifier.padding(horizontal = 4.dp),
            text = Strings.reportsFormulaEditorColorFormulaHelp,
            style = typography.bodySmall,
            color = colors.pageTextSubdued,
          )
        }

        Result(preview)
        FormulaShortcuts(field = field, text = text, queryNames = queryNames)
      }
    }
  }

@Composable
private fun Result(preview: FormulaData?) {
  val output = preview?.output
  if (output is Failure) {
    Text(
      modifier =
        Modifier.fillMaxWidth()
          .background(colors.errorBackground, CardShape)
          .padding(horizontal = 14.dp, vertical = 10.dp),
      text = output.displayString(),
      style = typography.bodySmall,
      color = colors.errorText,
    )
  } else {
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .background(colors.tableBackground, CardShape)
          .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        modifier = Modifier.weight(1f),
        text = Strings.reportsFormulaEditorEvaluatesTo,
        style = typography.bodySmall,
        color = colors.tableHeaderText,
      )
      if (preview == null || output == null) {
        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colors.pageText)
      } else {
        Text(
          modifier = Modifier.testTag(Tags.FormulaResult),
          text = output.displayString(),
          style = typography.titleMedium.copy(fontWeight = SemiBold),
          color = preview.resolveColor() ?: colors.pageText,
        )
      }
    }
  }

  preview?.warnings?.fastForEach { warning ->
    val message = warning.message()
    if (message != null) {
      Text(
        modifier = Modifier.padding(horizontal = 4.dp),
        text = message,
        style = typography.bodySmall,
        color = colors.pageTextSubdued,
      )
    }
  }
}

internal enum class FormulaField {
  Formula,
  ColorRule,
}

@Composable
private fun FormulaField.title(): String =
  when (this) {
    Formula -> Strings.reportsFormulaEditorFormula
    ColorRule -> Strings.reportsFormulaViewerColorRule
  }

private fun FormulaField.minHeight() =
  when (this) {
    Formula -> 220.dp
    ColorRule -> 120.dp
  }

@PortraitPreview
@Composable
private fun PreviewFormulaFieldScaffold(
  @PreviewParameter(FormulaFieldProvider::class) params: ColorParams<FormulaFieldParams>,
) =
  PreviewWithColorParams(params) {
    FormulaFieldScaffold(
      field = field,
      text = rememberTextFieldState(text),
      preview = preview,
      queryNames = PREVIEW_FORMULA_META.queries.keys.toImmutableList(),
      onBack = {},
      onDone = {},
    )
  }

private data class FormulaFieldParams(
  val field: FormulaField,
  val text: String,
  val preview: FormulaData?,
)

private class FormulaFieldProvider :
  ColorParamsProvider<FormulaFieldParams>(
    FormulaFieldParams(Formula, PREVIEW_FORMULA_META.formula.orEmpty(), PREVIEW_FORMULA_DATA),
    FormulaFieldParams(
      ColorRule,
      PREVIEW_FORMULA_META.colorFormula.orEmpty(),
      PREVIEW_FORMULA_DATA,
    ),
    FormulaFieldParams(
      Formula,
      "=SUM(1,",
      PREVIEW_FORMULA_DATA.copy(
        output = FormulaOutput.Failure(ErrorValue(Generic, UnexpectedEnd, null)),
      ),
    ),
    FormulaFieldParams(Formula, "", null),
  )
