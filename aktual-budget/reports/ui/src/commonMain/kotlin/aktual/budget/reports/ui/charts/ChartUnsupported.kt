package aktual.budget.reports.ui.charts

import aktual.budget.model.WidgetType
import aktual.budget.reports.ui.string
import aktual.budget.reports.vm.UnsupportedData
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
internal fun UnsupportedChart(data: UnsupportedData, modifier: Modifier = Modifier) =
  Column(modifier = modifier.padding(4.dp)) {
    val type = data.type.string()
    Text(text = data.name ?: type, color = colors.pageText, overflow = Ellipsis, maxLines = 1)
    if (data.name != null) {
      Text(text = type, color = colors.pageTextSubdued, overflow = Ellipsis, maxLines = 1)
    }

    Box(
      modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp),
      contentAlignment = Alignment.Center,
    ) {
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
  }

@Preview
@Composable
private fun PreviewUnsupportedChart(
  @PreviewParameter(UnsupportedChartProvider::class) params: ColoredParams<UnsupportedData>
) = PreviewWithColoredParams(params) { UnsupportedChart(data = params.data) }

private class UnsupportedChartProvider :
  ColoredParameterProvider<UnsupportedData>(
    UnsupportedData(Filters, WidgetType.CashFlow, name = "Groceries cash flow"),
    UnsupportedData(ReportType, WidgetType.Calendar, name = null),
  )
