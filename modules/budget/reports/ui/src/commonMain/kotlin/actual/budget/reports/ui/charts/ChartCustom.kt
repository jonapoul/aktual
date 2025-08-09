package actual.budget.reports.ui.charts

import actual.budget.reports.vm.CustomData
import actual.budget.reports.vm.ReportTimeRange
import actual.core.ui.ActualTypography
import actual.core.ui.LocalTheme
import actual.core.ui.Theme
import actual.l10n.Strings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

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
      style = ActualTypography.titleLarge,
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
