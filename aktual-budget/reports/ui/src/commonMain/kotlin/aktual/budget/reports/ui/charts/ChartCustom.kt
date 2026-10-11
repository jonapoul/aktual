package aktual.budget.reports.ui.charts

import aktual.budget.reports.vm.CustomData
import aktual.budget.reports.vm.CustomReportConfig
import aktual.budget.reports.vm.CustomReportDates
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.yearMonth

@Composable
internal fun CustomChart(
  data: CustomData,
  compact: Boolean,
  modifier: Modifier = Modifier,
  includeHeader: Boolean = true,
) =
  Column(modifier = modifier) {
    if (compact && includeHeader) {
      Header(modifier = Modifier.padding(4.dp).fillMaxWidth(), data = data)
    }

    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Center) {
      Text(
        text = Strings.reportsCustomNoop,
        style = typography.titleLarge,
        color = colors.warningText,
        textAlign = Center,
      )
    }
  }

@Composable
private fun Header(
  data: CustomData,
  modifier: Modifier = Modifier,
) =
  Row(modifier = modifier.fillMaxWidth()) {
    Text(
      modifier = Modifier.weight(1f),
      text = data.title,
      color = colors.pageText,
      overflow = Ellipsis,
    )

    DateRangeText(dateRange(data.config.dates))
  }

@Composable
private fun dateRange(dates: CustomReportDates): String =
  when (dates) {
    is Live -> dates.type.string()
    is Static -> dateRange(dates.start.yearMonth, dates.end.yearMonth)
  }

@Preview
@Composable
private fun PreviewCustomChart(
  @PreviewParameter(CustomChartProvider::class) params: ColoredParams<CustomChartParams>,
) =
  PreviewWithColoredParams(params, isPrivacyEnabled = params.data.isPrivacyEnabled) {
    CustomChart(
      modifier =
        Modifier.background(colors.tableBackground, CardShape)
          .width(WIDTH.dp)
          .let { m -> if (compact) m.height(300.dp) else m }
          .padding(5.dp),
      data = data,
      compact = compact,
    )
  }

private data class CustomChartParams(
  val data: CustomData,
  val compact: Boolean,
  val isPrivacyEnabled: Boolean = false,
)

private class CustomChartProvider :
  ColoredParameterProvider<CustomChartParams>(
    CustomChartParams(PREVIEW_CUSTOM_DATA, compact = false, isPrivacyEnabled = true),
    CustomChartParams(PREVIEW_CUSTOM_DATA, compact = false, isPrivacyEnabled = false),
    CustomChartParams(PREVIEW_CUSTOM_DATA, compact = true, isPrivacyEnabled = true),
    CustomChartParams(PREVIEW_CUSTOM_DATA, compact = true, isPrivacyEnabled = false),
  )

internal val PREVIEW_CUSTOM_DATA =
  CustomData(
    title = "My Custom Report",
    config =
      CustomReportConfig(
        mode = Total,
        groupBy = Category,
        balanceType = Payment,
        graphType = BarGraph,
        interval = Monthly,
        dates =
          CustomReportDates.Static(
            start = LocalDate(2011, Month.SEPTEMBER, 1),
            end = LocalDate(2025, Month.JULY, 31),
          ),
        showEmpty = false,
        showOffBudget = false,
        showHidden = false,
        showUncategorized = false,
        selectedCategories = persistentListOf(),
        conditions = persistentListOf(),
        conditionsOp = And,
        sortBy = Desc,
        trimIntervals = false,
        showTrendLines = false,
        colorScheme = null,
      ),
    range = LocalDate(2011, Month.SEPTEMBER, 1)..LocalDate(2025, Month.JULY, 31),
  )
