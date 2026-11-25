package aktual.budget.reports.vm

import aktual.budget.model.DateRangeType
import aktual.budget.model.GraphType
import aktual.budget.model.TagId
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.YearMonthRange

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
  fun `Budgeted can't split by tag`() {
    assertThat(DEFAULT.copy(balanceType = Budgeted).isGroupByEnabled(Tag)).isFalse()
    assertThat(DEFAULT.copy(groupBy = Tag).withBalanceType(Budgeted).groupBy).isEqualTo(Category)
    assertThat(DEFAULT.isGroupByEnabled(Tag)).isTrue()
  }

  @Test
  fun `Picking none or every tag covers all tags`() {
    val visible = listOf(TagId("a"), TagId("b"), TagId("c"))
    assertThat(DEFAULT.withTagScope(setOf(TagId("c"), TagId("a")), visible).tagScope)
      .isEqualTo(CustomTagScope.Selected(persistentListOf(TagId("a"), TagId("c"))))
    assertThat(DEFAULT.withTagScope(emptySet(), visible).tagScope).isEqualTo(All)
    assertThat(DEFAULT.withTagScope(visible.toSet(), visible).tagScope).isEqualTo(All)
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
  fun `Quarterly offers quarter ranges and longer`() {
    assertThat(dateRangeOptions(Quarterly))
      .containsExactly(
        DateRangeType.CurrentQuarter,
        DateRangeType.PreviousQuarter,
        DateRangeType.Last6Months,
        DateRangeType.Last12Months,
        DateRangeType.YearToDate,
        DateRangeType.LastYear,
        DateRangeType.PriorYearToDate,
        DateRangeType.AllTime,
      )
    assertThat(DEFAULT.withDateRange(ThisMonth).withInterval(Quarterly, Sunday).dates)
      .isEqualTo(CustomReportDates.Live(Last12Months, includeCurrent = false))
  }

  @Test
  fun `Quarterly static dates cover whole quarters`() {
    val quarterly = DEFAULT.withInterval(Quarterly, Sunday)
    assertThat(quarterly.withStaticMonths(YearMonth(2026, 2), YearMonth(2026, 5), Sunday).dates)
      .isEqualTo(CustomReportDates.Static(LocalDate(2026, 1, 1), LocalDate(2026, 6, 30)))

    val monthly = DEFAULT.withStaticMonths(YearMonth(2026, 2), YearMonth(2026, 5), Sunday)
    assertThat(monthly.withInterval(Quarterly, Sunday).dates)
      .isEqualTo(CustomReportDates.Static(LocalDate(2026, 1, 1), LocalDate(2026, 6, 30)))
  }

  @Test
  fun `Weekly static dates widen to whole weeks`() {
    val monthly = DEFAULT.withStaticMonths(YearMonth(2026, 3), YearMonth(2026, 5), Sunday)
    assertThat(monthly.withInterval(Weekly, Sunday).dates)
      .isEqualTo(CustomReportDates.Static(LocalDate(2026, 3, 1), LocalDate(2026, 6, 6)))
    assertThat(monthly.withInterval(Weekly, Monday).dates)
      .isEqualTo(CustomReportDates.Static(LocalDate(2026, 2, 23), LocalDate(2026, 5, 31)))
  }

  @Test
  fun `Weekly static dates on week boundaries don't change`() {
    val february = DEFAULT.withStaticMonths(YearMonth(2026, 2), YearMonth(2026, 2), Sunday)
    assertThat(february.withInterval(Weekly, Sunday).dates).isEqualTo(february.dates)
  }

  @Test
  fun `Picking months while weekly gives whole weeks without drifting`() {
    val weekly = DEFAULT.withInterval(Weekly, Sunday)
    val april = weekly.withStaticMonths(YearMonth(2026, 4), YearMonth(2026, 4), Sunday)
    assertThat(april.dates)
      .isEqualTo(CustomReportDates.Static(LocalDate(2026, 3, 29), LocalDate(2026, 5, 2)))
    assertThat(april.staticMonths(Sunday))
      .isEqualTo(YearMonthRange(YearMonth(2026, 4), YearMonth(2026, 4)))

    // Each edit sends back the months the pickers show, so the start stays put
    var edited = april
    repeat(2) { i ->
      val months = requireNotNull(edited.staticMonths(Sunday))
      edited = edited.withStaticMonths(months.start, YearMonth(2026, 5 + i), Sunday)
    }
    assertThat(edited.dates)
      .isEqualTo(CustomReportDates.Static(LocalDate(2026, 3, 29), LocalDate(2026, 7, 4)))
    assertThat(edited.staticMonths(Sunday))
      .isEqualTo(YearMonthRange(YearMonth(2026, 4), YearMonth(2026, 6)))
  }

  @Test
  fun `Changing interval keeps a range it offers`() {
    val live = DEFAULT.copy(dates = CustomReportDates.Live(YearToDate, includeCurrent = true))
    assertThat(live.withInterval(Weekly, Sunday).dates).isEqualTo(live.dates)
    assertThat(live.withInterval(Daily, Sunday).dates)
      .isEqualTo(CustomReportDates.Live(ThisMonth, includeCurrent = true))
  }

  @Test
  fun `Only past ranges can include the current period`() {
    assertThat(DEFAULT.withDateRange(Last3Months).canIncludeCurrent).isTrue()
    assertThat(DEFAULT.withDateRange(ThisMonth).canIncludeCurrent).isFalse()
    assertThat(DEFAULT.withStaticDates(RANGE, Sunday).canIncludeCurrent).isFalse()
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
    assertThat(DEFAULT.withStaticDates(RANGE, Sunday).dates)
      .isEqualTo(CustomReportDates.Static(LocalDate(2026, 3, 1), LocalDate(2026, 5, 31)))

    val moved = DEFAULT.withStaticMonths(YearMonth(2026, 6), YearMonth(2026, 4), Sunday)
    assertThat(moved.dates)
      .isEqualTo(CustomReportDates.Static(LocalDate(2026, 6, 1), LocalDate(2026, 6, 30)))
  }

  @Test
  fun `Live dates start from the interval's default`() {
    val yearly = DEFAULT.copy(interval = Yearly).withStaticDates(RANGE, Sunday)
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
