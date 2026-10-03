package aktual.budget.reports.vm

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.extracting
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month.DECEMBER
import kotlinx.datetime.Month.FEBRUARY
import kotlinx.datetime.Month.JANUARY
import kotlinx.datetime.YearMonth

// Ported from
// packages/desktop-client/src/components/reports/spreadsheets/age-of-money-spreadsheet.test.ts
class AgeOfMoneyTest {
  @Test
  fun `Simple FIFO match`() {
    val result = calculateAges([income("2024-01-01", 1000), expense("2024-01-15", 500)])

    assertThat(result.ages).extracting(ExpenseAge::age).containsExactly(14)
    assertThat(result.insufficientData).isFalse()
  }

  @Test
  fun `Oldest income is used first`() {
    val result =
      calculateAges(
        [income("2024-01-01", 500), income("2024-01-15", 500), expense("2024-02-01", 400)]
      )

    assertThat(result.ages).extracting(ExpenseAge::age).containsExactly(31)
  }

  @Test
  fun `Large expense spans buckets and uses the last one`() {
    val result =
      calculateAges(
        [income("2024-01-01", 200), income("2024-01-15", 300), expense("2024-02-01", 400)]
      )

    assertThat(result.ages).extracting(ExpenseAge::age).containsExactly(17)
  }

  @Test
  fun `Expenses consume buckets in order`() {
    val result =
      calculateAges(
        [
          income("2024-01-01", 1000),
          expense("2024-01-10", 300),
          expense("2024-01-20", 300),
          expense("2024-01-30", 300),
        ]
      )

    assertThat(result.ages).extracting(ExpenseAge::age).containsExactly(9, 19, 29)
    assertThat(result.insufficientData).isFalse()
  }

  @Test
  fun `Expenses beyond income are flagged`() {
    val result = calculateAges([income("2024-01-01", 100), expense("2024-01-15", 500)])

    assertThat(result.insufficientData).isTrue()
  }

  @Test
  fun `No income gives no ages`() {
    val result = calculateAges([expense("2024-01-15", 500)])

    assertThat(result.ages).isEmpty()
    assertThat(result.insufficientData).isTrue()
  }

  @Test
  fun `No expenses gives no ages`() {
    val result = calculateAges([income("2024-01-01", 1000)])

    assertThat(result.ages).isEmpty()
    assertThat(result.insufficientData).isFalse()
  }

  @Test
  fun `Same day income and expense is zero days old`() {
    val result = calculateAges([income("2024-01-15", 1000), expense("2024-01-15", 500)])

    assertThat(result.ages).extracting(ExpenseAge::age).containsExactly(0)
  }

  @Test
  fun `Transactions are sorted by date`() {
    val result =
      calculateAges(
        [
          income("2024-01-15", 500),
          income("2024-01-01", 500),
          expense("2024-02-15", 200),
          expense("2024-02-01", 200),
        ]
      )

    assertThat(result.ages)
      .containsExactly(
        ExpenseAge(LocalDate.parse("2024-02-01"), 31),
        ExpenseAge(LocalDate.parse("2024-02-15"), 45),
      )
  }

  @Test
  fun `Average of nothing is null`() {
    assertThat(averageAge([])).isNull()
  }

  @Test
  fun `Average uses the last ten ages and rounds`() {
    assertThat(averageAge([10, 20, 30])).isEqualTo(20)
    assertThat(averageAge([1, 2])).isEqualTo(2)
    assertThat(averageAge([100] + List(10) { 5 })).isEqualTo(5)
  }

  @Test
  fun `Trend compares the last two values`() {
    assertThat(calculateTrend([])).isEqualTo(Stable)
    assertThat(calculateTrend([10])).isEqualTo(Stable)
    assertThat(calculateTrend([10, 15])).isEqualTo(Up)
    assertThat(calculateTrend([15, 10])).isEqualTo(Down)
    assertThat(calculateTrend([10, 12])).isEqualTo(Stable)
    assertThat(calculateTrend([50, 10, 20])).isEqualTo(Up)
  }

  @Test
  fun `Daily periods stop at today`() {
    val ages = [age("2016-12-15", 5), age("2016-12-31", 10), age("2017-01-01", 12)]

    val data =
      calculateGraphData(
        ages,
        start = YearMonth(2016, DECEMBER),
        end = YearMonth(2017, JANUARY),
        granularity = Daily,
        today = LocalDate.parse("2017-01-01"),
      )

    // Dec 15 to Dec 31 plus Jan 1
    assertThat(data.size).isEqualTo(18)
    assertThat(data.keys.last()).isEqualTo(LocalDate.parse("2017-01-01"))
  }

  @Test
  fun `Weekly periods stop at the week containing today`() {
    val ages = [age("2016-12-15", 5), age("2017-01-01", 12)]

    val data =
      calculateGraphData(
        ages,
        start = YearMonth(2016, DECEMBER),
        end = YearMonth(2017, FEBRUARY),
        granularity = Weekly,
        today = LocalDate.parse("2017-01-01"),
      )

    assertThat(data.keys.toList())
      .containsExactly(
        LocalDate.parse("2016-12-12"),
        LocalDate.parse("2016-12-19"),
        LocalDate.parse("2016-12-26"),
      )
  }

  @Test
  fun `Monthly periods include the current month`() {
    val ages = [age("2016-12-15", 8), age("2017-01-01", 10)]

    val data =
      calculateGraphData(
        ages,
        start = YearMonth(2016, DECEMBER),
        end = YearMonth(2017, JANUARY),
        granularity = Monthly,
        today = LocalDate.parse("2017-01-01"),
      )

    assertThat(data.toList())
      .containsExactly(LocalDate.parse("2016-12-01") to 8, LocalDate.parse("2017-01-01") to 9)
  }

  @Test
  fun `Money sent off budget is spent and money coming back is new income`() {
    val result =
      calculateAges(
        [
          income("2024-01-01", 1000),
          expense("2024-01-10", 600),
          income("2024-02-01", 600),
          expense("2024-02-15", 700),
        ]
      )

    // The second expense drains the rest of January's income, then dips into February's
    assertThat(result.ages).extracting(ExpenseAge::age).containsExactly(9, 14)
    assertThat(result.insufficientData).isFalse()
  }

  private fun income(date: String, amount: Long) =
    AgeOfMoneyTransaction(LocalDate.parse(date), amount)

  private fun expense(date: String, amount: Long) =
    AgeOfMoneyTransaction(LocalDate.parse(date), -amount)

  private fun age(date: String, age: Int) = ExpenseAge(LocalDate.parse(date), age)
}
