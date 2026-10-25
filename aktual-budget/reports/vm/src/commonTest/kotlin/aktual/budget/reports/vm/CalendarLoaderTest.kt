package aktual.budget.reports.vm

import aktual.budget.BudgetSyncController
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.DatabaseTables.ACCOUNTS
import aktual.budget.db.dao.DatabaseTables.CATEGORIES
import aktual.budget.db.dao.DatabaseTables.CATEGORY_GROUPS
import aktual.budget.db.dao.DatabaseTables.CATEGORY_MAPPING
import aktual.budget.model.Amount
import aktual.budget.model.Condition
import aktual.budget.model.FirstDayOfWeek
import aktual.prefs.FormatPreferences
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.datetime.DayOfWeek.MONDAY
import kotlinx.datetime.DayOfWeek.SUNDAY
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive

class CalendarLoaderTest {
  @Test
  fun `Sums income and expenses per day`() = runLoaderTest { loader, sync ->
    sync.insertSpending()

    loader.calendar(meta()).test {
      val data = awaitItem() as CalendarData
      assertThat(data.title).isEqualTo(TITLE)
      assertThat(data.start).isEqualTo(MAY)
      assertThat(data.end).isEqualTo(MAY)
      assertThat(data.income).isEqualTo(Amount(1400))
      assertThat(data.expenses).isEqualTo(Amount(4500))
      assertThat(data.firstDayOfWeek).isEqualTo(MONDAY)

      val month = data.months.single()
      assertThat(month.income).isEqualTo(Amount(1400))
      assertThat(month.expenses).isEqualTo(Amount(4500))
      assertThat(month.day(10)).isEqualTo(CalendarDay(10, Amount(900), Amount(4500)))
      assertThat(month.day(12)).isEqualTo(CalendarDay(12, Amount(500), Zero))
      assertThat(month.day(11)).isEqualTo(CalendarDay(11, Zero, Zero))
    }
  }

  @Test
  fun `Keeps the transactions matching the filters`() = runLoaderTest { loader, sync ->
    sync.insertSpending()

    loader.calendar(meta(conditions = listOf(FOOD))).test {
      val data = awaitItem() as CalendarData
      assertThat(data.income).isEqualTo(Amount(900))
      assertThat(data.expenses).isEqualTo(Amount(2500))
      assertThat(data.months.single().day(10)).isEqualTo(CalendarDay(10, Amount(900), Amount(2500)))
    }
  }

  @Test
  fun `Weeks start on Monday`() = runLoaderTest { loader, _ ->
    loader.calendar(meta()).test {
      // 1st May 2026 is a Friday, so four blanks before it and none after the 31st
      val days = (awaitItem() as CalendarData).months.single().days.map { it.day }
      assertThat(days).containsExactly(*(List(4) { 0 } + (1..31)).toTypedArray())
    }
  }

  @Test
  fun `Weeks start on the first day of week preference`() =
    runLoaderTest(firstDayOfWeek = Sunday) { loader, _ ->
      loader.calendar(meta()).test {
        val data = awaitItem() as CalendarData
        assertThat(data.firstDayOfWeek).isEqualTo(SUNDAY)
        val days = data.months.single().days.map { it.day }
        assertThat(days).containsExactly(*(List(5) { 0 } + (1..31) + List(6) { 0 }).toTypedArray())
      }
    }

  @Test
  fun `One month per month in the range`() = runLoaderTest { loader, _ ->
    loader.calendar(meta(start = YearMonth(2026, 3))).test {
      val data = awaitItem() as CalendarData
      assertThat(data.months.map { it.month })
        .containsExactly(YearMonth(2026, 3), YearMonth(2026, 4), MAY)
    }
  }

  @Test
  fun `Missing time frame shows the current month`() = runLoaderTest { loader, _ ->
    loader.calendar(CalendarReportMeta(name = TITLE)).test {
      val data = awaitItem() as CalendarData
      assertThat(data.months.map { it.month }).containsExactly(YearMonth(2026, 6))
    }
  }

  private fun CalendarMonth.day(day: Int) = days.single { it.day == day }

  private suspend fun BudgetSyncController.insertSpending() {
    row(ACCOUNTS, "checking", "name" to "Checking", "offbudget" to false)
    row(CATEGORY_GROUPS, "spending", "name" to "Spending", "sort_order" to 1L)
    for ((id, name) in listOf("food" to "Food", "fuel" to "Fuel")) {
      row(CATEGORIES, id, "name" to name, "cat_group" to "spending", "sort_order" to 1L)
      row(CATEGORY_MAPPING, id, "transferId" to id)
    }

    transaction("plain", -1000, category = "food")
    transaction("split", -3500, isParent = true)
    transaction("split-food", -1500, category = "food", parent = "split")
    transaction("split-fuel", -2000, category = "fuel", parent = "split")
    transaction("deleted", -5000, category = "food", isDeleted = true)
    transaction("income", 900, category = "food")
    transaction("later-income", 500, category = "fuel", date = 20260512)
    transaction("next-month", -9, category = "food", date = 20260601)
  }

  private fun meta(start: YearMonth = MAY, conditions: List<Condition>? = null) =
    CalendarReportMeta(
      name = TITLE,
      conditions = conditions,
      timeFrame = TimeFrame(start, MAY, Static),
    )

  private fun runLoaderTest(
    firstDayOfWeek: FirstDayOfWeek = Monday,
    action: suspend BudgetDatabase.(ChartDataLoader, BudgetSyncController) -> Unit,
  ) = runSyncedDatabaseTest { scope, sync ->
    val contexts = TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler))
    val formatPreferences =
      object : FormatPreferences by TestFormatPreferences {
        override val firstDayOfWeek = fixed(firstDayOfWeek)
      }
    val loader = chartDataLoader(contexts, calendar = { TODAY }, formatPreferences)
    action(loader, sync)
  }

  private companion object {
    const val TITLE = "Calendar"
    val TODAY = LocalDate(2026, 6, 15)
    val MAY = YearMonth(2026, 5)
    val FOOD =
      Condition(
        field = Category,
        operator = OneOf,
        value = JsonArray(listOf(JsonPrimitive("food"))),
      )
  }
}
