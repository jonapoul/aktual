package aktual.budget.reports.ui.charts

import aktual.budget.model.DateRangeType
import aktual.budget.reports.vm.ChartDateConfig
import aktual.budget.reports.vm.DateRangeMode
import aktual.core.l10n.Strings
import aktual.core.model.ColorSchemeType
import aktual.core.ui.ColorSchemeParameters
import aktual.core.ui.LocalTheme
import aktual.core.ui.NormalTextButton
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.SlidingToggleButton
import aktual.core.ui.Theme
import aktual.core.ui.YearMonthPicker
import alakazam.kotlin.compose.HorizontalSpacer
import alakazam.kotlin.compose.VerticalSpacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.YearMonthRange

@Composable
internal fun ChartDateConfig(
  config: ChartDateConfig,
  onNewConfig: (ChartDateConfig) -> Unit,
  onDateRangeType: (DateRangeType) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Column(
  modifier = modifier,
  horizontalAlignment = Alignment.Start,
  verticalArrangement = Arrangement.Top,
) {
  val modes = remember { DateRangeMode.entries.toImmutableList() }
  var selectedIndex by remember { mutableIntStateOf(modes.indexOf(config.mode)) }
  SlidingToggleButton(
    modifier = Modifier.wrapContentSize(),
    options = modes,
    onSelectOption = { index -> selectedIndex = index },
    selectedIndex = selectedIndex,
    theme = theme,
    string = { type -> type.string() },
  )

  VerticalSpacer(SPACING)

  Row(
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = Strings.reportsDateConfigStart,
    )

    HorizontalSpacer(SPACING)

    YearMonthPicker(
      modifier = Modifier.weight(MONTH_PICKER_WEIGHT),
      value = config.start,
      range = config.range,
      theme = theme,
      onValueChange = { onNewConfig(config.copy(start = it)) },
    )
  }

  VerticalSpacer(SPACING)

  Row(
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = Strings.reportsDateConfigEnd,
    )

    HorizontalSpacer(SPACING)

    YearMonthPicker(
      modifier = Modifier.weight(MONTH_PICKER_WEIGHT),
      value = config.end,
      range = config.range,
      theme = theme,
      onValueChange = { onNewConfig(config.copy(end = it)) },
    )
  }

  VerticalSpacer(SPACING)

  FlowRow(
    modifier = Modifier.fillMaxWidth(),
    itemVerticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(SPACING),
  ) {
    DateRangeType.entries.fastForEach { type ->
      DateRangeTypeButton(
        type = type,
        onClick = onDateRangeType,
      )
    }
  }
}

@Composable
private fun DateRangeTypeButton(
  type: DateRangeType,
  onClick: (DateRangeType) -> Unit,
  modifier: Modifier = Modifier,
) = NormalTextButton(
  modifier = modifier,
  text = type.string(),
  onClick = { onClick(type) },
)

private const val MONTH_PICKER_WEIGHT = 1.5f
private val SPACING = 8.dp

@Composable
private fun DateRangeMode.string() = when (this) {
  DateRangeMode.Live -> Strings.reportsDateConfigLive
  DateRangeMode.Static -> Strings.reportsDateConfigStatic
}

@Preview
@Composable
private fun PreviewChartDateConfig(
  @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType,
) = PreviewWithColorScheme(type) {
  ChartDateConfig(
    modifier = Modifier.padding(8.dp),
    onNewConfig = {},
    onDateRangeType = {},
    config = ChartDateConfig(
      mode = DateRangeMode.Static,
      start = YearMonth(2025, Month.FEBRUARY),
      end = YearMonth(2025, Month.JULY),
      range = YearMonthRange(
        start = YearMonth(2011, Month.SEPTEMBER),
        endInclusive = YearMonth(2025, Month.JULY),
      ),
    ),
  )
}
