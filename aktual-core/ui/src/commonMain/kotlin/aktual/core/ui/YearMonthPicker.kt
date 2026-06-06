@file:Suppress("ComposeUnstableReceiver")

package aktual.core.ui

import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
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
  var selected by remember { mutableStateOf(value) }
  val displayText = selected.stringLong()
  val textState = rememberTextFieldState(initialText = displayText)
  LaunchedEffect(displayText) {
    if (textState.text.toString() != displayText) {
      textState.edit { replace(0, length, displayText) }
    }
  }

  AktualTextField(
    state = textState,
    modifier = modifier.wrapContentWidth().clickable { showDialog = true },
    readOnly = true,
    placeholderText = null,
    colors = colors.exposedDropDownMenu(),
  )

  if (showDialog) {
    PickDateDialog(
      value = selected,
      range = range,
      onDismiss = { showDialog = false },
      onValueChange = { newValue ->
        selected = newValue
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
      TextButton(onClick = onDismiss) {
        Text(text = Strings.yearMonthPickerCancel, color = colors.pageText)
      }

      TextButton(enabled = isWithinRange, onClick = { onValueChange(currentValue) }) {
        Text(
          text = Strings.yearMonthPickerSave,
          color = if (isWithinRange) colors.pageTextPositive else colors.pageTextSubdued,
        )
      }
    },
  ) {
    val centredTextStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
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
        textAlign = TextAlign.Center,
      )
    }
  }
}

private fun <T : Comparable<T>> YearMonthRange.rangeValues(
  picker: (YearMonth) -> T
): ImmutableList<T> = asSequence().map(picker).distinct().sorted().toImmutableList()

@Preview
@Composable
private fun PreviewYearMonthPicker(@PreviewParameter(ColoredParameters::class) colors: Colors) =
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
private fun PreviewDialogContent(@PreviewParameter(ColoredParameters::class) colors: Colors) =
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
  @PreviewParameter(ColoredParameters::class) colors: Colors
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
