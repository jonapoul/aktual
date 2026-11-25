package aktual.budget.reports.vm

import aktual.budget.model.BalanceType
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.Field
import aktual.budget.model.FirstDayOfWeek
import aktual.budget.model.Interval
import aktual.budget.model.Operator
import aktual.budget.rules.domain.CompiledTransactionFilter
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

// packages/desktop-client/src/components/reports/spreadsheets/countFilteredIntervals.test.ts
class FilteredIntervalsTest {
  @Test
  fun `Strict comparisons at month boundaries`() {
    val count =
      count(
        Monthly,
        START,
        END,
        listOf(date(GreaterThan, "2025-12-31"), date(LessThan, "2026-03-01")),
      )
    assertThat(count).isEqualTo(2)
  }

  @Test
  fun `Only months with a date satisfying every date condition`() {
    val count =
      count(
        Monthly,
        START,
        END,
        listOf(date(GreaterThanOrEquals, "2026-02-20"), date(LessThanOrEquals, "2026-02-10")),
      )
    assertThat(count).isEqualTo(0)
  }

  @Test
  fun `Each month counts once when alternatives overlap or have gaps`() {
    val count =
      count(
        Monthly,
        START,
        END,
        listOf(date(Is, "2026-01"), date(Is, "2026-01-14"), date(Is, "2026-03-01")),
        Or,
      )
    assertThat(count).isEqualTo(2)
  }

  @Test
  fun `A year filter is limited to the report months`() {
    assertThat(count(Monthly, START, END, listOf(date(Is, "2026")))).isEqualTo(4)
  }

  @Test
  fun `An approximate date match covers both months`() {
    assertThat(count(Monthly, START, END, listOf(date(IsApprox, "2026-02-01")))).isEqualTo(2)
  }

  @Test
  fun `Date filters combine with other fields`() {
    val conditions = listOf(date(Is, "2026-01"), condition(Payee, Is, JsonPrimitive("payee-id")))
    assertThat(count(Monthly, START, END, conditions, And)).isEqualTo(1)
    assertThat(count(Monthly, START, END, conditions, Or)).isEqualTo(5)
  }

  @Test
  fun `An empty category selection joined with OR doesn't widen the dates`() {
    val conditions =
      listOf(date(Is, "2026-01-15"), condition(Category, OneOf, JsonArray(emptyList())))
    assertThat(count(Monthly, START, END, conditions, Or)).isEqualTo(1)
  }

  @Test
  fun `July through September is one quarter`() {
    val count =
      count(
        Quarterly,
        "2025-10-01",
        "2026-12-31",
        listOf(date(GreaterThanOrEquals, "2026-07-01"), date(LessThanOrEquals, "2026-09-30")),
      )
    assertThat(count).isEqualTo(1)
  }

  @Test
  fun `A filter crossing a quarter boundary counts both quarters`() {
    val count =
      count(
        Quarterly,
        "2026-01-01",
        "2026-12-31",
        listOf(date(GreaterThanOrEquals, "2026-06-30"), date(LessThanOrEquals, "2026-07-01")),
      )
    assertThat(count).isEqualTo(2)
  }

  @Test
  fun `Is between applies both bounds`() {
    val conditions = listOf(between("2026-06-30", "2026-07-01"))
    assertThat(count(Monthly, "2025-10-01", "2027-03-31", conditions)).isEqualTo(2)
    assertThat(count(Quarterly, "2025-10-01", "2027-03-31", conditions)).isEqualTo(2)
  }

  @Test
  fun `Dates outside the report are left out even within its boundary quarter`() {
    val count =
      count(
        Quarterly,
        "2026-02-01",
        "2026-08-31",
        listOf(date(Is, "2026-01-31"), date(Is, "2026-09-01")),
        Or,
      )
    assertThat(count).isEqualTo(0)
  }

  @Test
  fun `The year covered by a date filter in a multi-year report`() {
    val count =
      count(Yearly, "2025-01-01", "2026-12-31", listOf(between("2026-07-01", "2026-09-30")))
    assertThat(count).isEqualTo(1)
  }

  @Test
  fun `Yearly reports use the whole boundary years`() {
    val count =
      count(
        Yearly,
        "2025-06-15",
        "2026-03-15",
        listOf(date(Is, "2025-01-01"), date(Is, "2026-12-31")),
        Or,
      )
    assertThat(count).isEqualTo(2)
  }

  @Test
  fun `Weeks use the report's first day of week`() {
    val conditions = listOf(between("2026-06-28", "2026-07-06"))
    assertThat(count(Weekly, "2026-06-01", "2026-07-31", conditions, firstDayOfWeek = Sunday))
      .isEqualTo(2)
    assertThat(count(Weekly, "2026-06-01", "2026-07-31", conditions, firstDayOfWeek = Monday))
      .isEqualTo(3)
  }

  @Test
  fun `Partial weeks are clipped to the report's days`() {
    val count =
      count(
        Weekly,
        "2026-06-30",
        "2026-07-09",
        listOf(date(Is, "2026-06-29"), date(Is, "2026-07-10")),
        Or,
        firstDayOfWeek = Monday,
      )
    assertThat(count).isEqualTo(0)
  }

  @Test
  fun `Individual days including a leap day`() {
    val count =
      count(
        Daily,
        "2024-02-28",
        "2024-03-01",
        listOf(date(GreaterThan, "2024-02-28"), date(LessThan, "2024-03-02")),
      )
    assertThat(count).isEqualTo(2)
  }

  @Test
  fun `No daily intervals when the report starts after it ends`() {
    assertThat(count(Daily, "2026-07-04", "2026-07-01", listOf(date(Is, "2026-07-01"))))
      .isEqualTo(0)
  }

  @Test
  fun `Stops after the last day of the four-digit years`() {
    assertThat(count(Daily, "9999-12-31", "9999-12-31", listOf(date(Is, "9999-12-30"))))
      .isEqualTo(0)
  }

  @Test
  fun `Without a date condition every interval counts`() {
    val count = count(Monthly, START, END, listOf(condition(Payee, Is, JsonPrimitive("payee-id"))))
    assertThat(count).isEqualTo(5)
  }

  @Test
  fun `Budgeted reports count every interval`() {
    val count = count(Monthly, START, END, listOf(date(Is, "2026-01")), balanceType = Budgeted)
    assertThat(count).isEqualTo(5)
  }

  @Test
  fun `Saved filters don't count as date conditions`() {
    val saved = date(Is, "2026-01").copy(customName = "January")
    assertThat(count(Monthly, START, END, listOf(saved))).isEqualTo(5)
  }

  private fun count(
    interval: Interval,
    start: String,
    end: String,
    conditions: List<Condition>,
    op: ConditionOp = And,
    firstDayOfWeek: FirstDayOfWeek = Sunday,
    balanceType: BalanceType = Payment,
  ): Int {
    val range = LocalDate.parse(start)..LocalDate.parse(end)
    val config =
      CONFIG.copy(
        interval = interval,
        balanceType = balanceType,
        conditions = conditions.toImmutableList(),
        conditionsOp = op,
      )
    return averagedIntervalCount(
      config,
      range,
      intervals(interval, range, firstDayOfWeek),
      CompiledTransactionFilter.compile(conditions, op),
    )
  }

  private companion object {
    const val START = "2025-12-15"
    const val END = "2026-04-15"

    val CONFIG = checkNotNull(emptyCustomReport().toConfig())

    fun condition(field: Field, operator: Operator, value: JsonElement) =
      Condition(field = field, operator = operator, value = value)

    fun date(operator: Operator, value: String) = condition(Date, operator, JsonPrimitive(value))

    fun between(from: String, to: String) =
      condition(
        Date,
        IsBetween,
        JsonObject(mapOf("num1" to JsonPrimitive(from), "num2" to JsonPrimitive(to))),
      )
  }
}
