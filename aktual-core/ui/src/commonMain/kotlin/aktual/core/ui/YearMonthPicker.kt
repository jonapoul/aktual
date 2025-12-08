@file:Suppress("ComposeUnstableReceiver")

package aktual.core.ui

import aktual.core.model.ColorSchemeType
import aktual.l10n.Strings
import alakazam.kotlin.compose.VerticalSpacer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
  theme: Theme = LocalTheme.current,
) {
  require(value in range) { "Given month $value is not in range $range" }

  var showDialog by remember { mutableStateOf(false) }
  var selected by remember { mutableStateOf(value) }

  TextField(
    modifier = modifier
      .wrapContentWidth()
      .clickable { showDialog = true },
    readOnly = true,
    placeholderText = null,
    value = selected.stringLong(),
    onValueChange = {},
    colors = theme.exposedDropDownMenu(),
    theme = theme,
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
  theme: Theme = LocalTheme.current,
) = BasicAlertDialog(
  modifier = modifier,
  onDismissRequest = onDismiss,
  content = {
    PickDateDialogContent(
      value = value,
      range = range,
      onDismiss = onDismiss,
      onValueChange = onValueChange,
      theme = theme,
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
  theme: Theme = LocalTheme.current,
) {
  var currentValue by remember { mutableStateOf(value) }
  val withinRange = currentValue in range

  DialogContent(
    modifier = modifier,
    theme = theme,
    title = Strings.yearMonthPickerTitle,
    buttons = {
      TextButton(onClick = onDismiss) {
        Text(text = Strings.yearMonthPickerCancel, color = theme.pageText)
      }

      TextButton(
        enabled = withinRange,
        onClick = { onValueChange(currentValue) },
      ) {
        Text(
          text = Strings.yearMonthPickerSave,
          color = if (withinRange) theme.pageTextPositive else theme.pageTextSubdued,
        )
      }
    },
  ) {
    ExposedDropDownMenu(
      value = currentValue.month,
      onValueChange = { currentValue = YearMonth(currentValue.year, it) },
      options = remember(range) { range.rangeValues { it.month } },
      theme = theme,
      string = { it.stringLong() },
    )

    VerticalSpacer(8.dp)

    ExposedDropDownMenu(
      value = currentValue.year,
      onValueChange = { currentValue = YearMonth(it, currentValue.month) },
      options = remember(range) { range.rangeValues { it.year } },
      theme = theme,
      string = { it.toString() },
    )

    if (!withinRange) {
      val start = range.start.stringShort()
      val end = range.endInclusive.stringShort()
      Text(
        modifier = Modifier.padding(8.dp),
        text = Strings.yearMonthPickerOutOfRange(start, end),
        color = theme.errorText,
        textAlign = TextAlign.Center,
      )
    }
  }
}

private fun <T : Comparable<T>> YearMonthRange.rangeValues(
  picker: (YearMonth) -> T,
): ImmutableList<T> = map { picker(it) }
  .distinct()
  .sorted()
  .toImmutableList()

@Preview
@Composable
private fun PreviewYearMonthPicker(
  @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType,
) = PreviewWithColorScheme(type) {
  YearMonthPicker(
    modifier = Modifier.padding(4.dp),
    value = YearMonth(2025, Month.FEBRUARY),
    onValueChange = {},
    range = YearMonthRange(
      start = YearMonth(2011, Month.DECEMBER),
      endInclusive = YearMonth(2025, Month.JULY),
    ),
  )
}

@Preview
@Composable
private fun PreviewDialogContent(
  @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType,
) = PreviewWithColorScheme(type) {
  PickDateDialogContent(
    value = YearMonth(2025, Month.FEBRUARY),
    range = YearMonthRange(
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
  @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType,
) = PreviewWithColorScheme(type) {
  PickDateDialogContent(
    value = YearMonth(2025, Month.AUGUST),
    range = YearMonthRange(
      start = YearMonth(2011, Month.DECEMBER),
      endInclusive = YearMonth(2025, Month.JULY),
    ),
    onDismiss = {},
    onValueChange = {},
  )
}
