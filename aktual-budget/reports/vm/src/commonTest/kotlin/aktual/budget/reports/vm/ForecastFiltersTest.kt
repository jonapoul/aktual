package aktual.budget.reports.vm

import aktual.budget.db.ForecastAccounts
import aktual.budget.db.ForecastPayees
import aktual.budget.model.AccountId
import aktual.budget.model.Condition
import aktual.budget.model.PayeeId
import aktual.budget.model.ScheduleId
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month.DECEMBER
import kotlinx.datetime.Month.JANUARY
import kotlinx.datetime.YearMonth
import kotlinx.serialization.json.JsonPrimitive

class ForecastFiltersTest {
  @Test
  fun `Account conditions narrow the accounts`() {
    val filters = ForecastFilters([IS_CHECKING], op = null)

    assertThat(filters.accounts(ACCOUNTS, selected = null)).containsOnly(CHECKING)
  }

  @Test
  fun `Account conditions don't narrow the accounts when any condition can match`() {
    val filters = ForecastFilters([IS_CHECKING, IS_EMPLOYER], op = Or)

    assertThat(filters.accounts(ACCOUNTS, selected = [SAVINGS])).containsOnly(SAVINGS)
  }

  @Test
  fun `Account conditions narrow the chosen accounts when all conditions must match`() {
    val filters = ForecastFilters([IS_CHECKING, IS_EMPLOYER], op = null)

    assertThat(filters.accounts(ACCOUNTS, selected = [CHECKING, SAVINGS])).containsOnly(CHECKING)
  }

  @Test
  fun `Schedule occurrences have to match the conditions`() {
    val filters = ForecastFilters([IS_EMPLOYER], op = null)

    assertThat(filters.matches(occurrence(payee = EMPLOYER), ACCOUNTS, PAYEES)).isTrue()
    assertThat(filters.matches(occurrence(payee = LANDLORD), ACCOUNTS, PAYEES)).isFalse()
  }

  @Test
  fun `Older widgets fall back to their start and end months`() {
    val meta = BalanceForecastReportMeta(startDate = "2025-01", endDate = "2025-12")

    assertThat(meta.forecastTimeFrame())
      .isEqualTo(TimeFrame(YearMonth(2025, JANUARY), YearMonth(2025, DECEMBER), Static))
    assertThat(BalanceForecastReportMeta(startDate = "nope").forecastTimeFrame()).isNull()
  }

  @Test
  fun `Transfer legs carry the source account's transfer payee`() {
    val schedule =
      ForecastSchedule(
        id = SCHEDULE,
        nextDate = null,
        date = ScheduleDate.Single(DATE),
        account = CHECKING,
        payee = SAVINGS_PAYEE,
        amount = -500,
        exactDate = false,
      )

    val occurrences =
      buildScheduleOccurrences(
        schedules = [schedule],
        end = DATE,
        transferAccounts = mapOf(SAVINGS_PAYEE to SAVINGS),
        postedDates = emptyMap(),
        transferPayees = mapOf(CHECKING to CHECKING_PAYEE),
      )

    assertThat(occurrences.map { Triple(it.account, it.payee, it.isTransfer) })
      .containsExactly(
        Triple(CHECKING, SAVINGS_PAYEE, false),
        Triple(SAVINGS, CHECKING_PAYEE, true),
      )
  }

  private fun occurrence(payee: PayeeId) =
    ForecastOccurrence(SCHEDULE, CHECKING, DATE, amount = 100, payee = payee)

  private companion object {
    val DATE = LocalDate(2026, 1, 10)
    val SCHEDULE = ScheduleId("schedule")
    val CHECKING = AccountId("checking")
    val SAVINGS = AccountId("savings")
    val EMPLOYER = PayeeId("employer")
    val LANDLORD = PayeeId("landlord")
    val CHECKING_PAYEE = PayeeId("checking-payee")
    val SAVINGS_PAYEE = PayeeId("savings-payee")
    val ACCOUNTS =
      [
        ForecastAccounts(CHECKING, "Checking", false),
        ForecastAccounts(SAVINGS, "Savings", false),
      ]
    val PAYEES =
      [
        ForecastPayees(EMPLOYER, "Employer", null),
        ForecastPayees(LANDLORD, "Landlord", null),
      ]
    val IS_CHECKING = Condition(field = Account, operator = Is, value = JsonPrimitive("checking"))
    val IS_EMPLOYER = Condition(field = Payee, operator = Is, value = JsonPrimitive("employer"))
  }
}
