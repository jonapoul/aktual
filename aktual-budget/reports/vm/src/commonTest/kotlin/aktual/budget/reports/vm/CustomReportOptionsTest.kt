package aktual.budget.reports.vm

import aktual.budget.model.DateRangeType
import aktual.budget.model.GraphType
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

class CustomReportOptionsTest {
  @Test
  fun `Bar button draws stacked bars in time mode`() {
    assertThat(DEFAULT.graphOptions)
      .containsExactly(
        GraphType.TableGraph,
        GraphType.BarGraph,
        GraphType.LineGraph,
        GraphType.AreaGraph,
      )
    assertThat(DEFAULT.copy(mode = Time).graphOptions)
      .containsExactly(
        GraphType.TableGraph,
        GraphType.StackedBarGraph,
        GraphType.LineGraph,
        GraphType.AreaGraph,
      )
  }

  @Test
  fun `Total mode can't draw lines, time mode can't draw areas`() {
    assertThat(DEFAULT.isGraphEnabled(LineGraph)).isFalse()
    assertThat(DEFAULT.isGraphEnabled(AreaGraph)).isTrue()
    assertThat(DEFAULT.copy(mode = Time).isGraphEnabled(LineGraph)).isTrue()
    assertThat(DEFAULT.copy(mode = Time).isGraphEnabled(AreaGraph)).isFalse()
  }

  @Test
  fun `Switching to time mode stacks the bars`() {
    assertThat(DEFAULT.withMode(Time)).all {
      prop(CustomReportConfig::mode).isEqualTo(Time)
      prop(CustomReportConfig::graphType).isEqualTo(StackedBarGraph)
    }
    assertThat(DEFAULT.withMode(Time).withMode(Total))
      .prop(CustomReportConfig::graphType)
      .isEqualTo(BarGraph)
  }

  @Test
  fun `Switching mode replaces a graph the new mode can't draw`() {
    val area = DEFAULT.copy(graphType = AreaGraph, groupBy = Interval)
    assertThat(area.withMode(Time)).all {
      prop(CustomReportConfig::graphType).isEqualTo(TableGraph)
      prop(CustomReportConfig::groupBy).isEqualTo(Category)
    }
  }

  @Test
  fun `Area graphs are always split by interval`() {
    val area = DEFAULT.withGraphType(AreaGraph)
    assertThat(area.groupBy).isEqualTo(Interval)
    assertThat(area.isGroupByEnabled(Category)).isFalse()
    assertThat(area.isGroupByEnabled(Interval)).isTrue()
  }

  @Test
  fun `Changing graph resets the sort`() {
    assertThat(DEFAULT.copy(sortBy = Name).withGraphType(TableGraph))
      .prop(CustomReportConfig::sortBy)
      .isEqualTo(Budget)
  }

  @Test
  fun `Time tables can't show net payments`() {
    val table = DEFAULT.copy(mode = Time, graphType = TableGraph, balanceType = NetPayment)
    assertThat(table.isBalanceTypeEnabled(NetPayment)).isFalse()
    assertThat(DEFAULT.copy(balanceType = NetPayment).withMode(Time).withGraphType(TableGraph))
      .prop(CustomReportConfig::balanceType)
      .isEqualTo(Payment)
  }

  @Test
  fun `Budgeted moves off payees and daily intervals`() {
    val daily =
      DEFAULT.copy(
        groupBy = Payee,
        interval = Daily,
        dates = CustomReportDates.Live(ThisWeek, includeCurrent = false),
      )
    assertThat(daily.withBalanceType(Budgeted)).all {
      prop(CustomReportConfig::groupBy).isEqualTo(Category)
      prop(CustomReportConfig::interval).isEqualTo(Monthly)
      prop(CustomReportConfig::dates).isEqualTo(CustomReportDates.Live(Last6Months, false))
    }

    val budgeted = DEFAULT.copy(balanceType = Budgeted)
    assertThat(budgeted.isGroupByEnabled(Payee)).isFalse()
    assertThat(budgeted.isIntervalEnabled(Weekly)).isFalse()
    assertThat(budgeted.isIntervalEnabled(Yearly)).isTrue()
  }

  @Test
  fun `Intervals only offer their own ranges`() {
    assertThat(dateRangeOptions(Daily))
      .containsExactly(
        DateRangeType.ThisWeek,
        DateRangeType.LastWeek,
        DateRangeType.ThisMonth,
        DateRangeType.LastMonth,
        DateRangeType.CurrentQuarter,
        DateRangeType.PreviousQuarter,
        DateRangeType.Last30Days,
        DateRangeType.Last3Months,
      )
    assertThat(dateRangeOptions(Yearly))
      .containsExactly(
        DateRangeType.YearToDate,
        DateRangeType.LastYear,
        DateRangeType.PriorYearToDate,
        DateRangeType.AllTime,
      )
  }

  @Test
  fun `Changing interval keeps a range it offers`() {
    val live = DEFAULT.copy(dates = CustomReportDates.Live(YearToDate, includeCurrent = true))
    assertThat(live.withInterval(Weekly).dates).isEqualTo(live.dates)
    assertThat(live.withInterval(Daily).dates)
      .isEqualTo(CustomReportDates.Live(ThisMonth, includeCurrent = true))
  }

  @Test
  fun `Only past ranges can include the current period`() {
    assertThat(DEFAULT.withDateRange(Last3Months).canIncludeCurrent).isTrue()
    assertThat(DEFAULT.withDateRange(ThisMonth).canIncludeCurrent).isFalse()
    assertThat(DEFAULT.withStaticDates(RANGE).canIncludeCurrent).isFalse()
  }

  @Test
  fun `Sort is hidden for interval splits and time graphs`() {
    assertThat(DEFAULT.showsSort).isTrue()
    assertThat(DEFAULT.copy(groupBy = Interval).showsSort).isFalse()
    assertThat(DEFAULT.withMode(Time).showsSort).isFalse()
    assertThat(DEFAULT.copy(groupBy = Interval, graphType = TableGraph).showsSort).isTrue()
  }

  @Test
  fun `Static dates cover whole months`() {
    assertThat(DEFAULT.withStaticDates(RANGE).dates)
      .isEqualTo(CustomReportDates.Static(LocalDate(2026, 3, 1), LocalDate(2026, 5, 31)))

    val moved = DEFAULT.withStaticMonths(YearMonth(2026, 6), YearMonth(2026, 4))
    assertThat(moved.dates)
      .isEqualTo(CustomReportDates.Static(LocalDate(2026, 6, 1), LocalDate(2026, 6, 30)))
  }

  @Test
  fun `Live dates start from the interval's default`() {
    val yearly = DEFAULT.copy(interval = Yearly).withStaticDates(RANGE)
    assertThat(yearly.withLiveDates().dates)
      .isEqualTo(CustomReportDates.Live(YearToDate, includeCurrent = true))
  }

  @Test
  fun `Trend lines are only for line graphs`() {
    assertThat(DEFAULT.canShowTrendLines).isFalse()
    assertThat(DEFAULT.withMode(Time).withGraphType(LineGraph).canShowTrendLines).isTrue()
  }

  private companion object {
    val DEFAULT = requireNotNull(emptyCustomReport().toConfig())
    val RANGE = LocalDate(2026, 3, 14)..LocalDate(2026, 5, 2)
  }
}
