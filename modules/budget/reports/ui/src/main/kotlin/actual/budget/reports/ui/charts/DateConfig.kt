package actual.budget.reports.ui.charts

import actual.budget.model.DateRangeType
import actual.budget.reports.vm.ChartDateConfig
import actual.budget.reports.vm.DateRangeMode
import actual.core.model.ColorSchemeType
import actual.core.ui.LocalTheme
import actual.core.ui.NormalTextButton
import actual.core.ui.PreviewWithColorScheme
import actual.core.ui.SlidingToggleButton
import actual.core.ui.Theme
import actual.core.ui.YearMonthPicker
import actual.l10n.Strings
import alakazam.android.ui.compose.HorizontalSpacer
import alakazam.android.ui.compose.VerticalSpacer
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

@Composable
private fun DateRangeType.string() = when (this) {
  DateRangeType.ThisWeek -> Strings.reportsDateTypeThisWeek
  DateRangeType.LastWeek -> Strings.reportsDateTypeLastWeek
  DateRangeType.ThisMonth -> Strings.reportsDateTypeThisMonth
  DateRangeType.LastMonth -> Strings.reportsDateTypeLastMonth
  DateRangeType.Last3Months -> Strings.reportsDateTypeLast3Months
  DateRangeType.Last6Months -> Strings.reportsDateTypeLast6Months
  DateRangeType.Last12Months -> Strings.reportsDateTypeLast12Months
  DateRangeType.YearToDate -> Strings.reportsDateTypeYearToDate
  DateRangeType.LastYear -> Strings.reportsDateTypeLastYear
  DateRangeType.AllTime -> Strings.reportsDateTypeAllTime
}

@Preview
@Composable
private fun PreviewLight() = PreviewConfig(ColorSchemeType.Light)

@Preview
@Composable
private fun PreviewDark() = PreviewConfig(ColorSchemeType.Dark)

@Preview
@Composable
private fun PreviewMidnight() = PreviewConfig(ColorSchemeType.Midnight)

@Composable
@Suppress("MagicNumber")
private fun PreviewConfig(type: ColorSchemeType) = PreviewWithColorScheme(type) {
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
