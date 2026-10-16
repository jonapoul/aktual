package aktual.budget.reports.ui.charts

import aktual.budget.model.Amount
import aktual.budget.model.GraphType
import aktual.budget.model.GroupBy
import aktual.budget.reports.vm.CustomMetric
import aktual.budget.reports.vm.CustomTotals
import aktual.budget.reports.vm.CustomTrend
import aktual.core.theme.Colors
import androidx.compose.ui.text.AnnotatedString
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.collections.immutable.persistentMapOf

class CustomLineChartsTest {
  private val theme = Colors.Fallback

  @Test
  fun `Trend series run from each group's start to its end`() {
    val base = previewCustomData(LineGraph, Category, Payment, showTrendLines = true)
    val first = base.groups[0].group
    val data = base.copy(trends = persistentMapOf(first to CustomTrend(Amount(0.0), Amount(50.0))))
    val legend = customBars(data, List(data.groups.size) { "$it" }, theme)

    val series = trendSeries(data, legend)

    assertThat(series.size).isEqualTo(data.groups.size)
    assertThat(series[0]).containsExactly(0.0, 10.0, 20.0, 30.0, 40.0, 50.0)
    assertThat(series[1]).isEqualTo(List(6) { 0.0 })
  }

  @Test
  fun `No trend series unless the report shows them`() {
    val data = previewCustomData(LineGraph, Category, Payment, showTrendLines = true)
    val legend = customBars(data, List(data.groups.size) { "$it" }, theme)
    val hidden = data.copy(config = data.config.copy(showTrendLines = false))
    assertThat(trendSeries(hidden, legend)).isEmpty()
  }

  @Test
  fun `Interval bars ignore the grouping`() {
    val data = previewCustomData(BarLineGraph, Category, Net)
    val bars = intervalBars(data, List(data.intervals.size) { "$it" }, theme)

    assertThat(bars.size).isEqualTo(6)
    assertThat(bars[0].amount).isEqualTo(Amount(1360.0))
    assertThat(bars[5].color).isEqualTo(theme.reportsNumberNegative)
  }

  @Test
  fun `Series per group can't be grouped by interval`() {
    fun supported(graph: GraphType, groupBy: GroupBy) =
      previewCustomData(graph, groupBy, Payment).config.isSupported()

    assertThat(supported(LineGraph, Category)).isTrue()
    assertThat(supported(LineGraph, Interval)).isFalse()
    assertThat(supported(StackedBarGraph, Interval)).isFalse()
    assertThat(supported(AreaGraph, Interval)).isTrue()
    assertThat(supported(AreaGraph, Category)).isTrue()
    assertThat(supported(BarLineGraph, Payee)).isTrue()
    assertThat(supported(TableGraph, Category)).isFalse()
    assertThat(supported(DonutGraph, Category)).isFalse()
  }

  @Test
  fun `Net marker shows assets, debts and the net total`() {
    val text = markerText(rows(Total))
    assertThat(text)
      .isEqualTo(
        """
        Jan 25
        Assets: 30.00
        Debts: -50.00
        Net: -20.00
        """
          .trimIndent(),
      )
  }

  @Test
  fun `Payment marker only shows debts`() {
    assertThat(markerText(rows(Debts))).isEqualTo("Jan 25\nDebts: -50.00")
    assertThat(markerText(rows(NetDebts))).isEqualTo("Jan 25\nNet debts: -20.00")
  }

  private fun rows(metric: CustomMetric) =
    totalsRows(
      metric = metric,
      assets = TotalsRow("Assets", Assets),
      debts = TotalsRow("Debts", Debts),
      netAssets = TotalsRow("Net assets", NetAssets),
      netDebts = TotalsRow("Net debts", NetDebts),
      net = TotalsRow("Net", Total),
    )

  private fun markerText(rows: List<TotalsRow>) =
    totalsMarkerText("Jan 25", TOTALS, rows) { AnnotatedString(it.toString()) }.text

  private companion object {
    val TOTALS =
      CustomTotals(
        assets = Amount(30.0),
        debts = Amount(-50.0),
        netAssets = Zero,
        netDebts = Amount(-20.0),
        total = Amount(-20.0),
      )
  }
}
