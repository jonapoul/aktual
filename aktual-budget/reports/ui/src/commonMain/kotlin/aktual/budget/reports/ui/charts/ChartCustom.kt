/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.reports.ui.charts

import aktual.budget.reports.ui.charts.WIDTH
import aktual.budget.reports.vm.CustomData
import aktual.budget.reports.vm.DateRangeMode
import aktual.budget.reports.vm.ReportTimeRange
import aktual.core.ui.AktualTypography
import aktual.core.ui.CardShape
import aktual.core.ui.LocalTheme
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.Theme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.l10n.Strings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.YearMonthRange

@Composable
internal fun CustomChart(
  data: CustomData,
  compact: Boolean,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
  includeHeader: Boolean = true,
) = Column(
  modifier = modifier,
) {
  if (compact && includeHeader) {
    Header(
      modifier = Modifier
        .padding(4.dp)
        .fillMaxWidth(),
      data = data,
      theme = theme,
    )
  }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .weight(1f),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = Strings.reportsCustomNoop,
      style = AktualTypography.titleLarge,
      color = theme.warningText,
      textAlign = TextAlign.Center,
    )
  }
}

@Composable
private fun Header(
  data: CustomData,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Row(
  modifier = modifier.fillMaxWidth(),
) {
  Text(
    modifier = Modifier.weight(1f),
    text = data.title,
    color = theme.pageText,
    overflow = TextOverflow.Ellipsis,
  )

  Text(
    text = dateRange(data.range),
    color = theme.pageTextSubdued,
    overflow = TextOverflow.Ellipsis,
  )
}

@Composable
private fun dateRange(timeRange: ReportTimeRange): String = when (timeRange) {
  is ReportTimeRange.Relative -> timeRange.type.string()
  is ReportTimeRange.Specific -> dateRange(timeRange.range.start, timeRange.range.endInclusive)
}

@Preview
@Composable
private fun PreviewCustomChart(
  @PreviewParameter(CustomChartProvider::class) params: ThemedParams<CustomChartParams>,
) = PreviewWithColorScheme(
  schemeType = params.type,
  isPrivacyEnabled = params.data.isPrivacyEnabled,
) {
  CustomChart(
    modifier = Modifier
      .background(LocalTheme.current.tableBackground, CardShape)
      .width(WIDTH.dp)
      .let { m -> if (params.data.compact) m.height(300.dp) else m }
      .padding(5.dp),
    data = params.data.data,
    compact = params.data.compact,
  )
}

private data class CustomChartParams(
  val data: CustomData,
  val compact: Boolean,
  val isPrivacyEnabled: Boolean = false,
)

private class CustomChartProvider : ThemedParameterProvider<CustomChartParams>(
  CustomChartParams(PREVIEW_CUSTOM_DATA, compact = false, isPrivacyEnabled = true),
  CustomChartParams(PREVIEW_CUSTOM_DATA, compact = false, isPrivacyEnabled = false),
  CustomChartParams(PREVIEW_CUSTOM_DATA, compact = true, isPrivacyEnabled = true),
  CustomChartParams(PREVIEW_CUSTOM_DATA, compact = true, isPrivacyEnabled = false),
)

internal val PREVIEW_CUSTOM_DATA = CustomData(
  title = "My Custom Report",
  mode = DateRangeMode.Live,
  range = ReportTimeRange.Specific(
    range = YearMonthRange(
      start = YearMonth(2011, Month.SEPTEMBER),
      endInclusive = YearMonth(2025, Month.JULY),
    ),
  ),
)
