package aktual.budget.reports.ui.report

import aktual.budget.reports.vm.QueryEditorData
import aktual.budget.reports.vm.TimeFrame
import aktual.budget.reports.vm.TimeFrameMode
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.YearMonthPicker
import aktual.core.ui.radioButton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.YearMonthRange

// A query with no time frame isn't filtered by date, so it shows as all time until changed
@Composable
internal fun TimeFrameSheet(
  timeFrame: TimeFrame?,
  data: QueryEditorData,
  onChange: (TimeFrame) -> Unit,
  onDismiss: () -> Unit,
) =
  QuerySheet(title = Strings.reportsFormulaQueryTimeFrame, onDismiss = onDismiss) {
    val selected = timeFrame?.mode ?: Full
    Column(modifier = Modifier.selectableGroup()) {
      TIME_FRAME_MODES.fastForEach { mode ->
        ModeRow(
          mode = mode,
          isSelected = mode == selected,
          onSelect = {
            val current = data.currentMonth
            onChange(timeFrame?.copy(mode = mode) ?: TimeFrame(current, current, mode))
          },
        )
        if (timeFrame != null && mode == selected && timeFrame.hasMonths()) {
          MonthPickers(timeFrame = timeFrame, data = data, onChange = onChange)
        }
      }
    }
  }

@Composable
private fun ModeRow(mode: TimeFrameMode, isSelected: Boolean, onSelect: () -> Unit) =
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .heightIn(min = 48.dp)
        .selectable(selected = isSelected, role = Role.RadioButton, onClick = onSelect),
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    RadioButton(selected = isSelected, onClick = null, colors = colors.radioButton())
    Text(text = mode.label(), style = typography.bodyLarge, color = colors.pageText)
  }

@Composable
private fun MonthPickers(
  timeFrame: TimeFrame,
  data: QueryEditorData,
  onChange: (TimeFrame) -> Unit,
) {
  val range =
    YearMonthRange(
      minOf(data.months.start, timeFrame.start),
      maxOf(data.months.endInclusive, timeFrame.end),
    )
  Row(
    modifier = Modifier.fillMaxWidth().padding(start = 32.dp, bottom = 8.dp),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Column(modifier = Modifier.weight(1f)) {
      SectionLabel(Strings.reportsFormulaQueryStart)
      YearMonthPicker(
        modifier = Modifier.fillMaxWidth(),
        value = timeFrame.start,
        range = range,
        onValueChange = { start ->
          onChange(timeFrame.copy(start = start, end = maxOf(start, timeFrame.end)))
        },
      )
    }
    Column(modifier = Modifier.weight(1f)) {
      SectionLabel(Strings.reportsFormulaQueryEnd)
      YearMonthPicker(
        modifier = Modifier.fillMaxWidth(),
        value = timeFrame.end,
        range = range,
        onValueChange = { end ->
          onChange(timeFrame.copy(start = minOf(end, timeFrame.start), end = end))
        },
      )
    }
  }
}

internal fun TimeFrame.hasMonths(): Boolean = mode == SlidingWindow || mode == Static

internal val TIME_FRAME_MODES = TimeFrameMode.entries.filter { it != Unknown }.toImmutableList()
