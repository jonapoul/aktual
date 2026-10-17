@file:Suppress("MagicNumber")

package aktual.budget.reports.ui.charts

import aktual.budget.formula.ErrorReason.InternalError
import aktual.budget.formula.ErrorValue
import aktual.budget.formula.FormulaWarning
import aktual.budget.formula.UnknownAccount
import aktual.budget.formula.UnknownQuery
import aktual.budget.model.Amount
import aktual.budget.reports.vm.FORMULA_THEME_PREFIX
import aktual.budget.reports.vm.FormulaData
import aktual.budget.reports.vm.FormulaOutput
import aktual.core.l10n.Strings
import aktual.core.theme.attributes
import aktual.core.theme.parseColor
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.ScaleToFitText
import aktual.core.ui.formattedString
import aktual.core.ui.redacted
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.toImmutableList

// packages/desktop-client/src/components/reports/FormulaResult.tsx
@Composable
internal fun FormulaChart(
  data: FormulaData,
  compact: Boolean,
  modifier: Modifier = Modifier,
  includeHeader: Boolean = true,
) =
  Column(modifier = modifier, horizontalAlignment = CenterHorizontally) {
    val title = data.title.takeIf { data.showTitle }
    if (compact && includeHeader && title != null) {
      Text(
        modifier = Modifier.fillMaxWidth(),
        text = title,
        color = colors.pageText,
        style = typography.bodyLarge,
        overflow = Ellipsis,
        maxLines = 1,
      )
    }

    val text = data.output.displayString()
    val color =
      data.resolveColor() ?: if (data.output is Failure) colors.errorText else colors.pageText
    val style = typography.displayLarge.copy(textAlign = Center, lineHeight = 1.em)
    Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(8.dp), contentAlignment = Center) {
      if (data.fontSizeMode == Static) {
        Text(
          text = text.redacted(),
          color = color,
          style = style.copy(fontSize = data.staticFontSize.sp),
        )
      } else {
        ScaleToFitText(
          modifier = Modifier.fillMaxSize(),
          text = text,
          style = style,
          maxLines = text.lines().size,
          color = color,
        )
      }
    }

    data.warnings.fastForEach { warning ->
      val message = warning.message()
      if (message != null) {
        Text(text = message, color = colors.pageTextSubdued, style = typography.bodySmall)
      }
    }
  }

@Composable
internal fun FormulaOutput.displayString(): String =
  when (this) {
    is Money -> amount.formattedString()
    is Text -> value
    is Failure -> error.message()
  }

// A theme_* placeholder or a CSS colour. Anything else is ignored, like upstream's invalid colours
@Composable
internal fun FormulaData.resolveColor(): Color? {
  val value = color ?: return null
  val theme = colors
  return remember(value, theme) {
    if (value.startsWith(FORMULA_THEME_PREFIX)) {
      theme.attributes()[value.removePrefix(FORMULA_THEME_PREFIX)]
    } else {
      runCatching { value.parseColor() }.getOrNull()
    }
  }
}

@Suppress("CyclomaticComplexMethod")
@Composable
private fun ErrorValue.message(): String {
  val reason = reason ?: return type.literal
  val subject = subject.orEmpty()
  val text =
    when (reason) {
      MissingEquals -> return Strings.reportsFormulaMissingEquals
      UnexpectedCharacter -> Strings.reportsFormulaUnexpectedCharacter(subject)
      UnexpectedToken -> Strings.reportsFormulaUnexpectedToken
      UnexpectedEnd -> Strings.reportsFormulaUnexpectedEnd
      UnknownErrorLiteral -> Strings.reportsFormulaUnknownErrorLiteral
      CellRange -> Strings.reportsFormulaCellRange
      NumberCoercion -> Strings.reportsFormulaNumberCoercion
      WrongArgCount -> Strings.reportsFormulaWrongArgCount
      WrongArgType -> Strings.reportsFormulaWrongArgType
      ValueTooLarge -> Strings.reportsFormulaValueTooLarge
      ValueTooSmall -> Strings.reportsFormulaValueTooSmall
      IntegerExpected -> Strings.reportsFormulaIntegerExpected
      NotFinite -> Strings.reportsFormulaNotFinite
      ArraySizeMismatch -> Strings.reportsFormulaArraySizeMismatch
      UnknownName -> Strings.reportsFormulaUnknownName(subject)
      UnknownFunction -> Strings.reportsFormulaUnknownFunction(subject)
      UnsupportedFunction -> Strings.reportsFormulaUnsupportedFunction(subject)
      NoConditionMet -> Strings.reportsFormulaNoConditionMet
      NoDefault -> Strings.reportsFormulaNoDefault
      DistinctSigns -> Strings.reportsFormulaDistinctSigns
      NegativeCount -> Strings.reportsFormulaNegativeCount
      NegativeLength -> Strings.reportsFormulaNegativeLength
      LessThanOne -> Strings.reportsFormulaLessThanOne
      TextTooLong -> Strings.reportsFormulaTextTooLong
      InvalidDate -> Strings.reportsFormulaInvalidDate
      DateOutOfRange -> Strings.reportsFormulaDateOutOfRange
      InvalidBudgetDimension -> Strings.reportsFormulaInvalidBudgetDimension(subject)
      DecimalsOutOfRange -> Strings.reportsFormulaDecimalsOutOfRange
      BadMode -> Strings.reportsFormulaBadMode
      StartAfterEnd -> Strings.reportsFormulaStartAfterEnd
      NoValues -> Strings.reportsFormulaNoValues
      SelectorTooLarge -> Strings.reportsFormulaSelectorTooLarge
      StartOutOfRange -> Strings.reportsFormulaStartOutOfRange
      PatternNotFound -> Strings.reportsFormulaPatternNotFound
      InternalError -> Strings.reportsFormulaInternalError(subject)
    }
  return "${type.literal}\n$text"
}

@Composable
internal fun FormulaWarning.message(): String? =
  when (this) {
    is UnknownQuery -> Strings.reportsFormulaWarningUnknownQuery(name)
    is UnknownAccount -> Strings.reportsFormulaWarningUnknownAccount(account)
    else -> null
  }

@Preview
@Composable
private fun PreviewFormulaChart(
  @PreviewParameter(FormulaChartProvider::class) params: ColoredParams<FormulaChartParams>,
) =
  PreviewWithColoredParams(params) {
    FormulaChart(
      modifier =
        Modifier.background(colors.tableBackground, CardShape)
          .width(WIDTH.dp)
          .height(160.dp)
          .padding(5.dp),
      data = data,
      compact = true,
    )
  }

private data class FormulaChartParams(val data: FormulaData)

private class FormulaChartProvider :
  ColoredParameterProvider<FormulaChartParams>(
    listOf(
        formulaPreview(FormulaOutput.Money(Amount(123456))),
        formulaPreview(FormulaOutput.Money(Amount(-5000)), color = "#e11d48"),
        formulaPreview(
          FormulaOutput.Money(Amount(250000)),
          color = "${FORMULA_THEME_PREFIX}numberPositive",
        ),
        formulaPreview(FormulaOutput.Text("Line one\nLine two")),
        formulaPreview(FormulaOutput.Money(Amount(100)), static = true, showTitle = false),
        formulaPreview(FormulaOutput.Failure(ErrorValue(Name, UnknownFunction, "NOPE"))),
        formulaPreview(
          FormulaOutput.Money(Amount(0)),
          warnings = listOf(UnknownQuery("Groceries")),
        ),
      )
      .map(::FormulaChartParams),
  )

internal val PREVIEW_FORMULA_DATA =
  formulaPreview(
    FormulaOutput.Money(Amount(123456)),
    color = "${FORMULA_THEME_PREFIX}numberPositive",
  )

private fun formulaPreview(
  output: FormulaOutput,
  color: String? = null,
  static: Boolean = false,
  showTitle: Boolean = true,
  warnings: List<FormulaWarning> = emptyList(),
) =
  FormulaData(
    title = "Formula",
    showTitle = showTitle,
    output = output,
    color = color,
    fontSizeMode = if (static) Static else Dynamic,
    staticFontSize = 24.0,
    warnings = warnings.toImmutableList(),
  )
