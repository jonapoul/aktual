@file:Suppress("ComposeUnstableReceiver")

package aktual.budget.reports.components

import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualAlertDialogContent
import aktual.core.ui.AktualExposedDropDownMenu
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColorParameters
import aktual.core.ui.DialogTextButton
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.exposedDropDownMenu
import aktual.core.ui.stringLong
import aktual.core.ui.stringShort
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.YearMonthRange

@Composable
fun YearMonthPicker(
  value: YearMonth,
  range: YearMonthRange,
  onValueChange: (YearMonth) -> Unit,
  modifier: Modifier = Modifier,
) {
  require(value in range) { "Given month $value is not in range $range" }

  var showDialog by remember { mutableStateOf(false) }
  val displayText = value.stringLong()
  val textState = rememberTextFieldState(initialText = displayText)
  SideEffect(displayText) {
    if (textState.text.toString() != displayText) {
      textState.edit { replace(0, length, displayText) }
    }
  }

  // The text field consumes taps, so a clickable on it never fires. Catch them on top instead
  Box(modifier = modifier.wrapContentWidth()) {
    AktualTextField(
      state = textState,
      readOnly = true,
      placeholderText = null,
      colors = colors.exposedDropDownMenu(),
    )
    Box(modifier = Modifier.matchParentSize().clickable { showDialog = true })
  }

  if (showDialog) {
    PickDateDialog(
      value = value,
      range = range,
      onDismiss = { showDialog = false },
      onValueChange = { newValue ->
        onValueChange(newValue)
        showDialog = false
      },
    )
  }
}

@Composable
private fun PickDateDialog(
  value: YearMonth,
  range: YearMonthRange,
  onDismiss: () -> Unit,
  onValueChange: (YearMonth) -> Unit,
  modifier: Modifier = Modifier,
) =
  AktualAlertDialog(
    modifier = modifier,
    onDismissRequest = onDismiss,
    content = {
      PickDateDialogContent(
        value = value,
        range = range,
        onDismiss = onDismiss,
        onValueChange = onValueChange,
      )
    },
  )

@Composable
internal fun PickDateDialogContent(
  value: YearMonth,
  range: YearMonthRange,
  onDismiss: () -> Unit,
  onValueChange: (YearMonth) -> Unit,
  modifier: Modifier = Modifier,
) {
  var currentValue by remember { mutableStateOf(value) }
  val isWithinRange = currentValue in range

  AktualAlertDialogContent(
    modifier = modifier,
    title = Strings.yearMonthPickerTitle,
    buttons = {
      DialogTextButton(text = Strings.yearMonthPickerCancel, onClick = onDismiss)

      DialogTextButton(
        kind = Primary,
        text = Strings.yearMonthPickerSave,
        isEnabled = isWithinRange,
        onClick = { onValueChange(currentValue) },
      )
    },
  ) {
    val centredTextStyle = LocalTextStyle.current.copy(textAlign = Center)
    AktualExposedDropDownMenu(
      modifier = Modifier.fillMaxWidth(),
      value = currentValue.month,
      onValueChange = { currentValue = YearMonth(currentValue.year, it) },
      options = remember(range) { range.rangeValues { it.month } },
      string = { it.stringLong() },
      textStyle = centredTextStyle,
    )

    VerticalSpacer(8.dp)

    AktualExposedDropDownMenu(
      modifier = Modifier.fillMaxWidth(),
      value = currentValue.year,
      onValueChange = { currentValue = YearMonth(it, currentValue.month) },
      options = remember(range) { range.rangeValues { it.year } },
      string = { it.toString() },
      textStyle = centredTextStyle,
    )

    if (!isWithinRange) {
      val start = range.start.stringShort()
      val end = range.endInclusive.stringShort()
      Text(
        modifier = Modifier.padding(8.dp),
        text = Strings.yearMonthPickerOutOfRange(start, end),
        color = colors.errorText,
        textAlign = Center,
      )
    }
  }
}

private fun <T : Comparable<T>> YearMonthRange.rangeValues(
  picker: (YearMonth) -> T,
): ImmutableList<T> = asSequence().map(picker).distinct().sorted().toImmutableList()

@Preview
@Composable
private fun PreviewYearMonthPicker(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    YearMonthPicker(
      modifier = Modifier.padding(4.dp),
      value = YearMonth(2025, Month.FEBRUARY),
      onValueChange = {},
      range =
        YearMonthRange(
          start = YearMonth(2011, Month.DECEMBER),
          endInclusive = YearMonth(2025, Month.JULY),
        ),
    )
  }

@Preview
@Composable
private fun PreviewDialogContent(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    PickDateDialogContent(
      value = YearMonth(2025, Month.FEBRUARY),
      range =
        YearMonthRange(
          start = YearMonth(2011, Month.DECEMBER),
          endInclusive = YearMonth(2025, Month.JULY),
        ),
      onDismiss = {},
      onValueChange = {},
    )
  }

@Preview
@Composable
private fun PreviewDialogContentOutOfRange(
  @PreviewParameter(ColorParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    PickDateDialogContent(
      value = YearMonth(2025, Month.AUGUST),
      range =
        YearMonthRange(
          start = YearMonth(2011, Month.DECEMBER),
          endInclusive = YearMonth(2025, Month.JULY),
        ),
      onDismiss = {},
      onValueChange = {},
    )
  }
