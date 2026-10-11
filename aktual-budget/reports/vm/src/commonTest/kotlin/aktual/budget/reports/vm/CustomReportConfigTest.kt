package aktual.budget.reports.vm

import aktual.budget.model.ReportDate
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month.JULY
import kotlinx.datetime.Month.SEPTEMBER
import kotlinx.datetime.YearMonth

class CustomReportConfigTest {
  @Test
  fun `Empty columns take upstream's defaults`() {
    assertThat(emptyCustomReport().toConfig())
      .isEqualTo(
        CustomReportConfig(
          mode = Total,
          groupBy = Category,
          balanceType = Payment,
          graphType = BarGraph,
          interval = Monthly,
          dates = CustomReportDates.Live(AllTime, includeCurrent = false),
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
      )
  }

  @Test
  fun `Stored columns are kept`() {
    val report =
      emptyCustomReport()
        .copy(
          mode = Time,
          group_by = Payee,
          balance_type = NetDeposit,
          graph_type = StackedBarGraph,
          interval = Weekly,
          date_range = Last3Months,
          include_current = true,
          show_empty = true,
          show_hidden = true,
          conditions_op = Or,
          sort_by = Name,
          trim_intervals = true,
          show_trend_lines = true,
          color_scheme = "scheme",
        )

    assertThat(report.toConfig())
      .isEqualTo(
        CustomReportConfig(
          mode = Time,
          groupBy = Payee,
          balanceType = NetDeposit,
          graphType = StackedBarGraph,
          interval = Weekly,
          dates = CustomReportDates.Live(Last3Months, includeCurrent = true),
          showEmpty = true,
          showOffBudget = false,
          showHidden = true,
          showUncategorized = false,
          selectedCategories = persistentListOf(),
          conditions = persistentListOf(),
          conditionsOp = Or,
          sortBy = Name,
          trimIntervals = true,
          showTrendLines = true,
          colorScheme = "scheme",
        ),
      )
  }

  @Test
  fun `Static dates with a month-only end cover the whole month`() {
    val report =
      emptyCustomReport()
        .copy(
          date_static = true,
          start_date = ReportDate.Month(YearMonth(2011, SEPTEMBER)),
          end_date = ReportDate.Month(YearMonth(2025, JULY)),
        )

    assertThat(report.toConfig())
      .isNotNull()
      .prop(CustomReportConfig::dates)
      .isEqualTo(CustomReportDates.Static(LocalDate(2011, SEPTEMBER, 1), LocalDate(2025, JULY, 31)))
  }

  @Test
  fun `Static dates with full dates are kept`() {
    val report =
      emptyCustomReport()
        .copy(
          date_static = true,
          start_date = ReportDate.Date(LocalDate(2011, SEPTEMBER, 14)),
          end_date = ReportDate.Date(LocalDate(2025, JULY, 3)),
        )

    assertThat(report.toConfig())
      .isNotNull()
      .prop(CustomReportConfig::dates)
      .isEqualTo(CustomReportDates.Static(LocalDate(2011, SEPTEMBER, 14), LocalDate(2025, JULY, 3)))
  }

  @Test
  fun `Static without dates is unreadable`() {
    val report =
      emptyCustomReport()
        .copy(date_static = true, start_date = ReportDate.Date(LocalDate(2011, SEPTEMBER, 14)))
    assertThat(report.toConfig()).isNull()
  }

  @Test
  fun `Unknown values are unreadable`() {
    val report = emptyCustomReport()
    assertThat(report.copy(mode = Unknown).toConfig()).isNull()
    assertThat(report.copy(group_by = Unknown).toConfig()).isNull()
    assertThat(report.copy(balance_type = Unknown).toConfig()).isNull()
    assertThat(report.copy(graph_type = Unknown).toConfig()).isNull()
    assertThat(report.copy(interval = Unknown).toConfig()).isNull()
    assertThat(report.copy(sort_by = Unknown).toConfig()).isNull()
    assertThat(report.copy(conditions_op = Unknown).toConfig()).isNull()
    assertThat(report.copy(date_range = Unknown).toConfig()).isNull()
  }

  @Test
  fun `Unknown date range is fine for static dates`() {
    val report =
      emptyCustomReport()
        .copy(
          date_static = true,
          date_range = Unknown,
          start_date = ReportDate.Date(LocalDate(2011, SEPTEMBER, 14)),
          end_date = ReportDate.Date(LocalDate(2025, JULY, 3)),
        )
    assertThat(report.toConfig()).isNotNull()
  }
}
