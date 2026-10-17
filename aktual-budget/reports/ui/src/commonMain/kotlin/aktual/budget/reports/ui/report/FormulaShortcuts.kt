package aktual.budget.reports.ui.report

import aktual.budget.reports.vm.FORMULA_THEME_PREFIX
import aktual.core.l10n.Strings
import aktual.core.theme.attributes
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily.Companion.Monospace
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

// Chips that insert common names at the cursor, so they don't need typing on a phone keyboard
@Composable
internal fun FormulaShortcuts(
  field: FormulaField,
  text: TextFieldState,
  queryNames: ImmutableList<String>,
) =
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    when (field) {
      Formula -> {
        if (queryNames.isNotEmpty()) {
          ShortcutGroup(label = Strings.reportsFormulaQueries) {
            queryNames.fastForEach { name ->
              ShortcutChip(
                text = "\"$name\"",
                highlight = true,
                onClick = { text.insert("\"$name\"") },
              )
            }
          }
        }
        ShortcutGroup(label = Strings.reportsFormulaEditorFunctions) {
          FUNCTIONS.fastForEach { function ->
            // Leaves the cursor between the brackets, ready for the arguments
            ShortcutChip(
              text = "$function()",
              onClick = { text.insert("$function()", cursor = function.length + 1) },
            )
          }
        }
      }
      ColorRule -> {
        ShortcutGroup(label = Strings.reportsFormulaEditorVariables) {
          ShortcutChip(text = RESULT, highlight = true, onClick = { text.insert(RESULT) })
        }
        ShortcutGroup(label = Strings.reportsFormulaEditorThemeColors) {
          val theme = colors.attributes()
          THEME_COLORS.fastForEach { name ->
            val variable = "$FORMULA_THEME_PREFIX$name"
            ShortcutChip(text = variable, dot = theme[name], onClick = { text.insert(variable) })
          }
        }
      }
    }
  }

@Composable
private fun ShortcutGroup(label: String, chips: @Composable () -> Unit) =
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(
      modifier = Modifier.padding(horizontal = 4.dp),
      text = label.uppercase(),
      style = typography.labelSmall.copy(fontWeight = SemiBold, letterSpacing = 0.08.em),
      color = colors.tableHeaderText,
    )
    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      chips()
    }
  }

@Composable
private fun ShortcutChip(
  text: String,
  onClick: () -> Unit,
  highlight: Boolean = false,
  dot: Color? = null,
) {
  val accent = colors.buttonPrimaryBackground
  Row(
    modifier =
      Modifier.height(CHIP_HEIGHT)
        .clip(CHIP_SHAPE)
        .background(if (highlight) accent.copy(alpha = HIGHLIGHT_ALPHA) else colors.tableBackground)
        .border(1.dp, if (highlight) accent else colors.tableBorder, CHIP_SHAPE)
        .clickable(onClick = onClick)
        .padding(horizontal = 12.dp),
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    if (dot != null) {
      Box(modifier = Modifier.size(10.dp).background(dot, CircleShape))
    }
    Text(
      text = text,
      style = typography.bodySmall.copy(fontFamily = Monospace),
      color = colors.tableText,
    )
  }
}

// Replaces any selected text, and puts the cursor [cursor] characters into the inserted text
private fun TextFieldState.insert(value: String, cursor: Int = value.length) = edit {
  val start = selection.min
  replace(start, selection.max, value)
  selection = TextRange(start + cursor)
}

private const val RESULT = "RESULT"
private const val HIGHLIGHT_ALPHA = 0.18f
private val CHIP_HEIGHT = 36.dp
private val CHIP_SHAPE = RoundedCornerShape(10.dp)

private val FUNCTIONS =
  persistentListOf(
    "QUERY",
    "QUERY_COUNT",
    "BALANCE_OF",
    "BUDGET_QUERY",
    "SUM",
    "IF",
    "ROUND",
    "ABS",
    "MIN",
    "MAX",
  )

private val THEME_COLORS =
  persistentListOf("numberPositive", "numberNegative", "numberNeutral", "warningText", "errorText")
