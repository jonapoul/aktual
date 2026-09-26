package aktual.budget.reports.ui.charts

import aktual.budget.reports.vm.UnsupportedData
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
internal fun UnsupportedChart(data: UnsupportedData, modifier: Modifier = Modifier) =
  Box(modifier = modifier.padding(16.dp), contentAlignment = Alignment.Center) {
    Text(
      text =
        when (data.reason) {
          Filters -> Strings.reportsUnsupportedFilters
          ReportType -> Strings.reportsUnsupportedType
        },
      color = colors.pageTextSubdued,
      textAlign = TextAlign.Center,
    )
  }

@Preview
@Composable
private fun PreviewUnsupportedChart(
  @PreviewParameter(UnsupportedChartProvider::class) params: ColoredParams<UnsupportedData>
) = PreviewWithColoredParams(params) { UnsupportedChart(data = params.data) }

private class UnsupportedChartProvider :
  ColoredParameterProvider<UnsupportedData>(UnsupportedData(Filters), UnsupportedData(ReportType))
