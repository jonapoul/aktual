package aktual.budget.reports.vm

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.Condition
import aktual.budget.model.ConditionSet
import aktual.budget.model.PayeeId
import aktual.budget.rules.domain.ConditionsMatcher
import aktual.budget.rules.domain.FilterRow
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.serialization.json.JsonPrimitive

class CalendarDrilldownTest {
  @Test
  fun `A day matches only its own transactions`() {
    val matcher = matcher(data())

    assertThat(matcher.matches(row())).isTrue()
    assertThat(matcher.matches(row(date = LocalDate(2026, 2, 2)))).isFalse()
    assertThat(matcher.matches(row(date = LocalDate(2026, 2, 4)))).isFalse()
  }

  @Test
  fun `A day keeps the report's own conditions`() {
    val payeeIsShop = Condition(field = Payee, operator = Is, value = JsonPrimitive(SHOP.value))
    val matcher = matcher(data(ConditionSet(listOf(payeeIsShop))))

    assertThat(matcher.matches(row())).isTrue()
    assertThat(matcher.matches(row(payee = PayeeId("other")))).isFalse()
  }

  private fun matcher(data: CalendarData) = ConditionsMatcher(data.dayFilter(DAY))

  private fun data(conditions: ConditionSet? = null) =
    calculateCalendar(
      title = "Calendar",
      range = YearMonth(2026, 2)..YearMonth(2026, 2),
      transactions = emptyList(),
      firstDayOfWeek = Monday,
      conditions = conditions,
    )

  private fun row(date: LocalDate = DAY, payee: PayeeId = SHOP) =
    FilterRow(date = date, amount = Amount(-500), account = AccountId("checking"), payee = payee)

  private companion object {
    val DAY = LocalDate(2026, 2, 3)
    val SHOP = PayeeId("shop")
  }
}
