package aktual.budget.reports.vm

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.PayeeId
import aktual.budget.model.RecurConfig
import aktual.budget.model.RecurEndMode
import aktual.budget.model.RecurFrequency
import aktual.budget.model.RecurPattern
import aktual.budget.model.RecurType
import aktual.budget.model.ScheduleId
import aktual.budget.model.WeekendSolveMode
import aktual.budget.model.occurrences
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month.FEBRUARY
import kotlinx.datetime.Month.JANUARY
import kotlinx.datetime.Month.MARCH
import kotlinx.datetime.YearMonth

class BalanceForecastTest {
  @Test
  fun `Monthly schedules skip months without the day`() {
    val config = recur(Monthly, start = LocalDate(2026, 1, 31))

    assertThat(config.occurrences(until = LocalDate(2026, 5, 31)).dates)
      .containsExactly(
        LocalDate(2026, 1, 31),
        LocalDate(2026, 3, 31),
        LocalDate(2026, 5, 31),
      )
  }

  @Test
  fun `Monthly patterns combine days of the month and weekdays`() {
    val config =
      recur(
        Monthly,
        start = LocalDate(2026, 1, 1),
        patterns = listOf(RecurPattern(-1, RecurType.Day), RecurPattern(2, RecurType.Monday)),
      )

    assertThat(config.occurrences(until = LocalDate(2026, 2, 28)).dates)
      .containsExactly(
        LocalDate(2026, 1, 12),
        LocalDate(2026, 1, 31),
        LocalDate(2026, 2, 9),
        LocalDate(2026, 2, 28),
      )
  }

  @Test
  fun `Weekly interval and occurrence count`() {
    val config =
      recur(
        Weekly,
        start = LocalDate(2026, 1, 5),
        interval = 2,
        endMode = AfterNOccurrences,
        endOccurrences = 3,
      )

    val occurrences = config.occurrences(until = LocalDate(2026, 12, 31))
    assertThat(occurrences.dates)
      .containsExactly(LocalDate(2026, 1, 5), LocalDate(2026, 1, 19), LocalDate(2026, 2, 2))
    assertThat(occurrences.exhausted).isEqualTo(true)
  }

  @Test
  fun `End date is inclusive`() {
    val config =
      recur(Daily, start = LocalDate(2026, 1, 1), endMode = OnDate, endDate = LocalDate(2026, 1, 3))

    assertThat(config.occurrences(until = LocalDate(2026, 1, 31)).dates)
      .containsExactly(LocalDate(2026, 1, 1), LocalDate(2026, 1, 2), LocalDate(2026, 1, 3))
  }

  @Test
  fun `Weekend occurrences move to the next Monday or previous Friday`() {
    // 2026-02-01 is a Sunday
    val after = schedule(recur(Monthly, LocalDate(2026, 2, 1), skipWeekend = true, solve = After))
    val before = schedule(recur(Monthly, LocalDate(2026, 2, 1), skipWeekend = true, solve = Before))

    assertThat(futureOccurrenceDates(after, end = LocalDate(2026, 3, 31)))
      .containsExactly(LocalDate(2026, 2, 1), LocalDate(2026, 2, 2), LocalDate(2026, 3, 2))
    assertThat(futureOccurrenceDates(before, end = LocalDate(2026, 3, 31)))
      .containsExactly(LocalDate(2026, 2, 1), LocalDate(2026, 1, 30), LocalDate(2026, 2, 27))
  }

  @Test
  fun `Recurring occurrences start from the next date`() {
    val schedule =
      schedule(recur(Monthly, LocalDate(2025, 6, 15)), nextDate = LocalDate(2026, 1, 15))

    assertThat(futureOccurrenceDates(schedule, end = LocalDate(2026, 3, 31)))
      .containsExactly(LocalDate(2026, 1, 15), LocalDate(2026, 2, 15), LocalDate(2026, 3, 15))
  }

  @Test
  fun `Posted occurrences are skipped, allowing for early payments`() {
    val schedule = schedule(recur(Monthly, LocalDate(2026, 1, 15)))

    val occurrences =
      buildScheduleOccurrences(
        schedules = listOf(schedule),
        end = LocalDate(2026, 3, 31),
        transferAccounts = emptyMap(),
        postedDates = mapOf(SCHEDULE to listOf(LocalDate(2026, 2, 13))),
      )

    assertThat(occurrences.map { it.date })
      .containsExactly(LocalDate(2026, 1, 15), LocalDate(2026, 3, 15))
  }

  @Test
  fun `Exact date schedules only match posted transactions on the day`() {
    val schedule = schedule(recur(Monthly, LocalDate(2026, 1, 15)), exactDate = true)

    val occurrences =
      buildScheduleOccurrences(
        schedules = listOf(schedule),
        end = LocalDate(2026, 2, 28),
        transferAccounts = emptyMap(),
        postedDates = mapOf(SCHEDULE to listOf(LocalDate(2026, 2, 13))),
      )

    assertThat(occurrences.map { it.date })
      .containsExactly(LocalDate(2026, 1, 15), LocalDate(2026, 2, 15))
  }

  @Test
  fun `Transfer schedules add the opposite amount to the other account`() {
    val schedule =
      schedule(ScheduleDate.Single(LocalDate(2026, 1, 10)), payee = SAVINGS_PAYEE, amount = -500)

    val occurrences =
      buildScheduleOccurrences(
        schedules = listOf(schedule),
        end = LocalDate(2026, 1, 31),
        transferAccounts = mapOf(SAVINGS_PAYEE to SAVINGS),
        postedDates = emptyMap(),
      )

    assertThat(occurrences.map { it.account to it.amount })
      .containsExactly(CHECKING to -500L, SAVINGS to 500L)
  }

  @Test
  fun `Monthly forecast adds posted and scheduled transactions to the starting balance`() {
    val data =
      calculateBalanceForecast(
        params = params(end = YearMonth(2026, MARCH), today = LocalDate(2026, 1, 20)),
        accounts = setOf(CHECKING),
        startingBalances = mapOf(CHECKING to 1000L, SAVINGS to 99_999L),
        dailyTotals = mapOf(LocalDate(2026, 1, 5) to -200L),
        occurrences =
          listOf(
            // Before today, so already covered by posted transactions
            occurrence(LocalDate(2026, 1, 15), amount = -50),
            occurrence(LocalDate(2026, 2, 15), amount = -300),
            occurrence(LocalDate(2026, 3, 1), amount = 500),
            // Not a selected account
            occurrence(LocalDate(2026, 3, 1), amount = 700, account = SAVINGS),
          ),
      )

    assertThat(data.items)
      .isEqualTo(
        mapOf(
          LocalDate(2026, 1, 1) to Amount(800L),
          LocalDate(2026, 2, 1) to Amount(500L),
          LocalDate(2026, 3, 1) to Amount(1000L),
        )
      )
    assertThat(data.scheduledCount).isEqualTo(2)
  }

  @Test
  fun `Daily forecast has a point for every day`() {
    val data =
      calculateBalanceForecast(
        params =
          params(
            end = YearMonth(2026, FEBRUARY),
            today = LocalDate(2026, 1, 1),
            granularity = Daily,
          ),
        accounts = setOf(CHECKING, null),
        startingBalances = mapOf(CHECKING to 100L),
        dailyTotals = emptyMap(),
        occurrences = listOf(occurrence(LocalDate(2026, 2, 3), amount = -40, account = null)),
      )

    assertThat(data.items.size).isEqualTo(59)
    assertThat(data.items[LocalDate(2026, 2, 2)]).isEqualTo(Amount(100L))
    assertThat(data.items[LocalDate(2026, 2, 3)]).isEqualTo(Amount(60L))
  }

  @Test
  fun `No accounts means no points`() {
    val data =
      calculateBalanceForecast(
        params = params(end = YearMonth(2026, FEBRUARY), today = LocalDate(2026, 1, 1)),
        accounts = emptySet(),
        startingBalances = mapOf(CHECKING to 100L),
        dailyTotals = emptyMap(),
        occurrences = emptyList(),
      )

    assertThat(data.items).isEmpty()
  }

  @Test
  fun `Tracking budget forecast adds budgeted income and takes away budgeted expenses`() {
    val data =
      calculateTrackingBudgetForecast(
        params = params(end = YearMonth(2026, MARCH), today = LocalDate(2026, 1, 1)),
        onBudgetBalance = 1000L,
        budgetedIncome = mapOf(YearMonth(2026, JANUARY) to 500L, YearMonth(2026, MARCH) to 500L),
        budgetedExpenses =
          mapOf(YearMonth(2026, JANUARY) to 800L, YearMonth(2026, FEBRUARY) to 100L),
      )

    assertThat(data.items)
      .isEqualTo(
        mapOf(
          LocalDate(2026, 1, 1) to Amount(700L),
          LocalDate(2026, 2, 1) to Amount(600L),
          LocalDate(2026, 3, 1) to Amount(1100L),
        )
      )
  }

  @Test
  fun `Amounts between two values use the rounded average`() {
    assertThat(parseScheduleAmount("-1500")).isEqualTo(-1500L)
    assertThat(parseScheduleAmount("""{"num1":-1000,"num2":-2001}""")).isEqualTo(-1500L)
    assertThat(parseScheduleAmount(null)).isNull()
  }

  @Test
  fun `Schedule dates are either a plain date or a recurring config`() {
    assertThat(parseScheduleDate("2026-01-15"))
      .isEqualTo(ScheduleDate.Single(LocalDate(2026, 1, 15)))
    assertThat(parseScheduleDate("""{"start":"2026-01-15","frequency":"monthly","interval":1}"""))
      .isEqualTo(ScheduleDate.Recurring(recur(Monthly, LocalDate(2026, 1, 15), interval = 1)))
  }

  private fun recur(
    frequency: RecurFrequency,
    start: LocalDate,
    interval: Int? = null,
    patterns: List<RecurPattern>? = null,
    skipWeekend: Boolean? = null,
    solve: WeekendSolveMode? = null,
    endMode: RecurEndMode? = null,
    endOccurrences: Int? = null,
    endDate: LocalDate? = null,
  ) =
    RecurConfig(
      frequency = frequency,
      start = start,
      interval = interval,
      patterns = patterns,
      skipWeekend = skipWeekend,
      endMode = endMode,
      endOccurrences = endOccurrences,
      endDate = endDate,
      weekendSolveMode = solve,
    )

  private fun schedule(
    config: RecurConfig,
    nextDate: LocalDate = config.start,
    exactDate: Boolean = false,
  ) = schedule(ScheduleDate.Recurring(config), nextDate = nextDate, exactDate = exactDate)

  private fun schedule(
    date: ScheduleDate,
    nextDate: LocalDate? = null,
    payee: PayeeId? = null,
    amount: Long = -100,
    exactDate: Boolean = false,
  ) =
    ForecastSchedule(
      id = SCHEDULE,
      nextDate = nextDate,
      date = date,
      account = CHECKING,
      payee = payee,
      amount = amount,
      exactDate = exactDate,
    )

  private fun occurrence(date: LocalDate, amount: Long, account: AccountId? = CHECKING) =
    ForecastOccurrence(SCHEDULE, account, date, amount)

  private fun params(
    end: YearMonth,
    today: LocalDate,
    granularity: ForecastGranularity = Monthly,
  ) =
    ForecastParams(
      title = null,
      start = YearMonth(2026, JANUARY),
      end = end,
      granularity = granularity,
      today = today,
    )

  private companion object {
    val SCHEDULE = ScheduleId("schedule")
    val CHECKING = AccountId("checking")
    val SAVINGS = AccountId("savings")
    val SAVINGS_PAYEE = PayeeId("savings-payee")
  }
}
