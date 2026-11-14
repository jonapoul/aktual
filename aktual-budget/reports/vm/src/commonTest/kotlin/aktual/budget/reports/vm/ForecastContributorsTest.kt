package aktual.budget.reports.vm

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.PayeeId
import aktual.budget.model.ScheduleId
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isZero
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month.JANUARY
import kotlinx.datetime.Month.MARCH
import kotlinx.datetime.YearMonth

class ForecastContributorsTest {
  @Test
  fun `Ranks schedules by total impact and reconciles the ending balance`() {
    val data =
      calculateBalanceForecast(
        params = PARAMS,
        accounts = setOf(CHECKING),
        startingBalances = mapOf(CHECKING to 100_000L),
        dailyTotals = mapOf(LocalDate(2026, 1, 1) to 10_000L, LocalDate(2026, 3, 31) to -5_000L),
        occurrences =
          listOf(
            occurrence(SALARY, LocalDate(2026, 1, 1), 50_000),
            occurrence(RENT, LocalDate(2026, 1, 1), -30_000),
            occurrence(SALARY, LocalDate(2026, 2, 1), 50_000),
            occurrence(RENT, LocalDate(2026, 2, 1), -30_000),
            occurrence(BILL, LocalDate(2026, 2, 15), -1_000),
            // Outside the range
            occurrence(BILL, LocalDate(2026, 4, 15), -1_000),
          ),
      )

    assertThat(data.contributors).isNotNull().all {
      prop(ForecastContributors::openingBalance).isEqualTo(Amount(100_000L))
      prop(ForecastContributors::recordedNet).isEqualTo(Amount(5_000L))
      prop(ForecastContributors::scheduledNet).isEqualTo(Amount(39_000L))
      prop(ForecastContributors::endingBalance).isEqualTo(Amount(144_000L))
      prop(ForecastContributors::inflows)
        .containsExactly(contributor(SALARY, "Salary", 100_000, count = 2))
      prop(ForecastContributors::outflows)
        .containsExactly(
          contributor(RENT, "Rent", -60_000, count = 2),
          contributor(BILL, "Bill", -1_000, count = 1),
        )
      prop(ForecastContributors::internalTransferCount).isZero()
    }
    assertThat(data.items.values.last()).isEqualTo(Amount(144_000L))
  }

  @Test
  fun `Transfers between included accounts cancel out`() {
    val schedule =
      ForecastSchedule(
        id = TRANSFER,
        name = "Savings",
        nextDate = null,
        date = ScheduleDate.Single(LocalDate(2026, 1, 10)),
        account = CHECKING,
        payee = SAVINGS_PAYEE,
        amount = -500,
        exactDate = false,
      )
    val occurrences =
      buildScheduleOccurrences(
        schedules = listOf(schedule),
        end = LocalDate(2026, 3, 31),
        transferAccounts = mapOf(SAVINGS_PAYEE to SAVINGS),
        postedDates = emptyMap(),
      )

    fun contributors(accounts: Set<AccountId?>) =
      calculateBalanceForecast(
          params = PARAMS,
          accounts = accounts,
          startingBalances = emptyMap(),
          dailyTotals = emptyMap(),
          occurrences = occurrences,
        )
        .contributors

    assertThat(contributors(setOf(CHECKING, SAVINGS))).isNotNull().all {
      prop(ForecastContributors::inflows).isEmpty()
      prop(ForecastContributors::outflows).isEmpty()
      prop(ForecastContributors::scheduledNet).isEqualTo(Amount(0L))
      prop(ForecastContributors::internalTransferCount).isEqualTo(1)
    }

    // Only one leg is included, so it counts
    assertThat(contributors(setOf(CHECKING))).isNotNull().all {
      prop(ForecastContributors::outflows)
        .containsExactly(contributor(TRANSFER, "Savings", -500, count = 1, isTransfer = true))
      prop(ForecastContributors::internalTransferCount).isZero()
    }
  }

  @Test
  fun `Schedules without an account are flagged`() {
    val data =
      calculateBalanceForecast(
        params = PARAMS,
        accounts = setOf(CHECKING, null),
        startingBalances = emptyMap(),
        dailyTotals = emptyMap(),
        occurrences = listOf(occurrence(BILL, LocalDate(2026, 2, 1), -40, account = null)),
      )

    assertThat(data.contributors?.outflows)
      .isNotNull()
      .containsExactly(contributor(BILL, "Bill", -40, count = 1, isAccountless = true))
  }

  @Test
  fun `Forecasts without accounts or from the tracking budget have no contributors`() {
    val empty =
      calculateBalanceForecast(
        params = PARAMS,
        accounts = emptySet(),
        startingBalances = emptyMap(),
        dailyTotals = emptyMap(),
        occurrences = emptyList(),
      )
    val tracking =
      calculateTrackingBudgetForecast(
        params = PARAMS,
        onBudgetBalance = 0L,
        budgetedIncome = emptyMap(),
        budgetedExpenses = emptyMap(),
      )

    assertThat(empty.contributors).isNull()
    assertThat(tracking.contributors).isNull()
  }

  private fun occurrence(
    id: ScheduleId,
    date: LocalDate,
    amount: Long,
    account: AccountId? = CHECKING,
  ) =
    ForecastOccurrence(
      scheduleId = id,
      account = account,
      date = date,
      amount = amount,
      scheduleName = id.value.replaceFirstChar { it.uppercase() },
    )

  private fun contributor(
    id: ScheduleId,
    name: String,
    amount: Long,
    count: Int,
    isTransfer: Boolean = false,
    isAccountless: Boolean = false,
  ) = ForecastContributor(id, name, Amount(amount), count, isTransfer, isAccountless)

  private companion object {
    val PARAMS =
      ForecastParams(
        title = null,
        start = YearMonth(2026, JANUARY),
        end = YearMonth(2026, MARCH),
        granularity = Monthly,
        today = LocalDate(2026, 1, 1),
      )
    val SALARY = ScheduleId("salary")
    val RENT = ScheduleId("rent")
    val BILL = ScheduleId("bill")
    val TRANSFER = ScheduleId("transfer")
    val CHECKING = AccountId("checking")
    val SAVINGS = AccountId("savings")
    val SAVINGS_PAYEE = PayeeId("savings-payee")
  }
}
