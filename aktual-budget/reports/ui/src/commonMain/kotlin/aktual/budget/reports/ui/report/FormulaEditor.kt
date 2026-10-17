package aktual.budget.reports.ui.report

import aktual.budget.reports.ui.Action
import aktual.budget.reports.ui.ActionListener
import aktual.budget.reports.ui.montecarlo.LabeledCheckbox
import aktual.budget.reports.ui.montecarlo.LabeledField
import aktual.budget.reports.ui.montecarlo.NumberField
import aktual.budget.reports.vm.FontSizeMode
import aktual.budget.reports.vm.FormulaEdits
import aktual.budget.reports.vm.FormulaReportMeta
import aktual.core.l10n.Strings
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun FormulaEditor(
  meta: FormulaReportMeta,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
) {
  val formula = rememberTextFieldState(meta.formula.orEmpty())
  val colorFormula = rememberTextFieldState(meta.colorFormula.orEmpty())
  var mode by remember { mutableStateOf(meta.fontSizeMode.orDynamic()) }
  var size by remember { mutableDoubleStateOf(meta.staticFontSize ?: DEFAULT_STATIC_SIZE) }
  var showTitle by remember { mutableStateOf(meta.showTitle != false) }

  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    LabeledField(label = Strings.reportsFormulaEditorFormula) {
      AktualTextField(
        modifier = Modifier.fillMaxWidth(),
        state = formula,
        placeholderText = Strings.reportsFormulaEditorFormulaPlaceholder,
      )
    }

    LabeledField(
      label = Strings.reportsFormulaEditorColorFormula,
      help = Strings.reportsFormulaEditorColorFormulaHelp,
    ) {
      AktualTextField(
        modifier = Modifier.fillMaxWidth(),
        state = colorFormula,
        placeholderText = null,
      )
    }

    LabeledField(label = Strings.reportsFormulaEditorFontSize) {
      AktualSlidingToggleButton(
        selected = mode,
        options = persistentListOf(FontSizeMode.Dynamic, FontSizeMode.Static),
        onSelect = { mode = it },
        string = { option ->
          if (option == Static) {
            Strings.reportsFormulaEditorFontStatic
          } else {
            Strings.reportsFormulaEditorFontDynamic
          }
        },
      )
    }

    if (mode == Static) {
      LabeledField(label = Strings.reportsFormulaEditorStaticSize) {
        NumberField(
          value = size,
          min = MIN_STATIC_SIZE,
          max = MAX_STATIC_SIZE,
          onCommit = { size = it ?: DEFAULT_STATIC_SIZE },
        )
      }
    }

    LabeledCheckbox(
      label = Strings.reportsFormulaEditorShowTitle,
      checked = showTitle,
      onCheckedChange = { showTitle = it },
    )

    TextButton(
      modifier = Modifier.fillMaxWidth(),
      onClick = {
        onAction(
          Action.SaveFormula(
            FormulaEdits(
              formula = formula.text.toString().trim(),
              colorFormula = colorFormula.text.toString().trim(),
              fontSizeMode = mode,
              staticFontSize = size,
              showTitle = showTitle,
            ),
          ),
        )
      },
    ) {
      Text(text = Strings.reportsFormulaEditorSave, color = colors.numberPositive)
    }
  }
}

private fun FontSizeMode?.orDynamic(): FontSizeMode = if (this == Static) Static else Dynamic

private const val DEFAULT_STATIC_SIZE = 32.0
private const val MIN_STATIC_SIZE = 1.0
private const val MAX_STATIC_SIZE = 200.0
