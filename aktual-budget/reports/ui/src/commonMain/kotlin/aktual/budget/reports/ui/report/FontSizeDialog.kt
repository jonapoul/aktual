package aktual.budget.reports.ui.report

import aktual.budget.reports.ui.Tags
import aktual.budget.reports.ui.charts.FormulaChart
import aktual.budget.reports.ui.charts.PREVIEW_FORMULA_DATA
import aktual.budget.reports.vm.FontSizeMode
import aktual.budget.reports.vm.FormulaData
import aktual.core.l10n.Strings
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualAlertDialogContent
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.ColorParams
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.slider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun FontSizeDialog(
  initialMode: FontSizeMode,
  initialSize: Double,
  preview: FormulaData?,
  onApply: (FontSizeMode, Double) -> Unit,
  onDismiss: () -> Unit,
) {
  var mode by rememberSaveable { mutableStateOf(initialMode) }
  var size by rememberSaveable { mutableDoubleStateOf(initialSize.coerceIn(MIN_SIZE, MAX_SIZE)) }
  val sizeText = rememberTextFieldState(size.toSizeText())

  // Typed sizes only count once they're in range, so the slider doesn't jump while typing
  LaunchedEffect(sizeText) {
    snapshotFlow { sizeText.text.toString().toDoubleOrNull() }
      .collect { typed -> if (typed != null && typed in MIN_SIZE..MAX_SIZE) size = typed }
  }

  AktualAlertDialog(onDismissRequest = onDismiss) {
    FontSizeDialogContent(
      mode = mode,
      size = size,
      sizeText = sizeText,
      preview = preview,
      onSelectMode = { mode = it },
      onSlide = { slid ->
        size = slid
        sizeText.setTextAndPlaceCursorAtEnd(slid.toSizeText())
      },
      onApply = { onApply(mode, size) },
      onDismiss = onDismiss,
    )
  }
}

@Composable
private fun FontSizeDialogContent(
  mode: FontSizeMode,
  size: Double,
  sizeText: TextFieldState,
  preview: FormulaData?,
  onSelectMode: (FontSizeMode) -> Unit,
  onSlide: (Double) -> Unit,
  onApply: () -> Unit,
  onDismiss: () -> Unit,
) =
  AktualAlertDialogContent(
    title = Strings.reportsFormulaEditorFontSize,
    buttons = {
      TextButton(onClick = onDismiss) { Text(Strings.reportsFormulaEditorCancel) }
      TextButton(modifier = Modifier.testTag(Tags.ApplyFontSize), onClick = onApply) {
        Text(
          text = Strings.reportsFormulaEditorApply,
          fontWeight = Bold,
          color = colors.numberPositive,
        )
      }
    },
  ) {
    AktualSlidingToggleButton(
      modifier = Modifier.fillMaxWidth(),
      selected = mode,
      options = persistentListOf(Dynamic, Static),
      onSelect = onSelectMode,
      string = { option ->
        if (option == Static) {
          Strings.reportsFormulaEditorFontStatic
        } else {
          Strings.reportsFormulaEditorFontDynamic
        }
      },
    )

    if (mode == Static) {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = CenterVertically) {
          Text(
            modifier = Modifier.weight(1f),
            text = Strings.reportsFormulaEditorStaticSize,
            style = typography.bodyMedium,
            color = colors.pageTextSubdued,
          )
          AktualTextField(
            modifier = Modifier.width(SIZE_FIELD_WIDTH).testTag(Tags.FontSizeField),
            state = sizeText,
            placeholderText = null,
            singleLine = true,
            textStyle = typography.bodyMedium.copy(textAlign = Center),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = Done),
          )
        }
        Slider(
          modifier = Modifier.fillMaxWidth(),
          value = size.toFloat(),
          valueRange = MIN_SIZE.toFloat()..MAX_SIZE.toFloat(),
          onValueChange = { onSlide(it.roundToInt().toDouble()) },
          colors = colors.slider(),
        )
      }
    }

    if (preview != null) {
      Box(
        modifier =
          Modifier.fillMaxWidth()
            .height(PREVIEW_HEIGHT)
            .clipToBounds()
            .background(colors.tableBackground, CardShape),
      ) {
        FormulaChart(
          data = preview.copy(fontSizeMode = mode, staticFontSize = size),
          compact = false,
        )
      }
    }
  }

private fun Double.toSizeText(): String = roundToInt().toString()

private const val MIN_SIZE = 1.0
private const val MAX_SIZE = 200.0
private val SIZE_FIELD_WIDTH = 80.dp
private val PREVIEW_HEIGHT = 96.dp

@Preview
@Composable
private fun PreviewFontSizeDialog(
    @PreviewParameter(FontSizeDialogProvider::class) params: ColorParams<FontSizeMode>,
) =
  PreviewWithColorParams(params) {
    FontSizeDialogContent(
      mode = this,
      size = 32.0,
      sizeText = rememberTextFieldState("32"),
      preview = PREVIEW_FORMULA_DATA,
      onSelectMode = {},
      onSlide = {},
      onApply = {},
      onDismiss = {},
    )
  }

private class FontSizeDialogProvider : ColorParamsProvider<FontSizeMode>(Static, Dynamic)
