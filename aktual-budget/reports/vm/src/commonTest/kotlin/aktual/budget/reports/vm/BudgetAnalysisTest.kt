package aktual.budget.reports.vm

import aktual.budget.budgeting.domain.BudgetMonth
import aktual.budget.budgeting.domain.CategoryMonth
import aktual.budget.model.Amount
import aktual.budget.model.Amount.Companion.Zero
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.budget.model.Condition
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.Month.FEBRUARY
import kotlinx.datetime.Month.JANUARY
import kotlinx.datetime.Month.MARCH
import kotlinx.datetime.YearMonth
import kotlinx.serialization.json.JsonPrimitive

class BudgetAnalysisTest {
  @Test
  fun `Balance starts from what the month before carries over`() {
    val data =
      calculate(
        month(DEC, food(budgeted = 100, spent = -60, balance = 40)),
        month(JAN, food(budgeted = 100, spent = -80, balance = 60)),
      )

    assertThat(data.items[JAN])
      .isEqualTo(
        BudgetAnalysisDatum(
          budgeted = Amount(100),
          spent = Amount(-80),
          overspendingAdjustment = Zero,
          balance = Amount(60),
        ),
      )
    assertThat(data.items.keys.toList()).containsExactly(JAN)
  }

  @Test
  fun `Overspending without rollover is reset and shows as next month's adjustment`() {
    val data =
      calculate(
        month(DEC),
        month(JAN, food(budgeted = 100, spent = -150, balance = -50)),
        month(FEB, food(budgeted = 100, spent = -100, balance = 0)),
      )

    assertThat(data.items[JAN]?.balance).isEqualTo(Amount(-50))
    assertThat(data.items[FEB]?.overspendingAdjustment).isEqualTo(Amount(50))
    assertThat(data.items[FEB]?.balance).isEqualTo(Zero)
    assertThat(data.totalOverspendingAdjustment).isEqualTo(Amount(50))
  }

  @Test
  fun `Overspending with rollover carries into next month`() {
    val data =
      calculate(
        month(DEC),
        month(JAN, food(budgeted = 100, spent = -150, balance = -50, carryover = true)),
        month(FEB, food(budgeted = 100, spent = -100, balance = -50, carryover = true)),
      )

    assertThat(data.items[FEB]?.overspendingAdjustment).isEqualTo(Zero)
    assertThat(data.items[FEB]?.balance).isEqualTo(Amount(-50))
  }

  @Test
  fun `Months without budget data pass the balance through`() {
    val data =
      calculate(
        month(DEC),
        month(JAN, food(budgeted = 100, spent = -40, balance = 60)),
        month(FEB, food()),
        month(MAR, food(budgeted = 10, spent = 0, balance = 70)),
      )

    assertThat(data.items[FEB]?.balance).isEqualTo(Amount(60))
    assertThat(data.items[MAR]?.balance).isEqualTo(Amount(70))
    assertThat(data.endingBalance).isEqualTo(Amount(70))
  }

  @Test
  fun `Income and hidden categories are left out unless hidden ones are shown`() {
    val months =
      arrayOf(
        month(DEC),
        month(
          JAN,
          food(budgeted = 100, spent = -50, balance = 50),
          category(INCOME, budgeted = 0, spent = 1000, balance = 0, isIncome = true),
          category(HIDDEN, budgeted = 20, spent = 0, balance = 20, isHidden = true),
        ),
      )

    assertThat(calculate(*months).totalBudgeted).isEqualTo(Amount(100))
    assertThat(calculate(*months, params = PARAMS.copy(showHiddenCategories = true)).totalBudgeted)
      .isEqualTo(Amount(120))
  }

  @Test
  fun `Category conditions pick which categories count`() {
    val months =
      arrayOf(
        month(DEC),
        month(
          JAN,
          food(budgeted = 100, spent = -50, balance = 50),
          category(RENT, budgeted = 900, spent = -900, balance = 0),
        ),
      )
    val condition = Condition(field = Category, operator = Is, value = JsonPrimitive(RENT.value))

    val data = calculate(*months, params = PARAMS.copy(conditions = listOf(condition)))

    assertThat(data.totalBudgeted).isEqualTo(Amount(900))
    assertThat(data.totalSpent).isEqualTo(Amount(-900))
  }

  @Test
  fun `Tracking budgets reset positive balances without carryover`() {
    val data =
      calculate(
        trackingMonth(DEC, food(budgeted = 100, spent = -60, balance = 40)),
        trackingMonth(JAN, food(budgeted = 100, spent = -80, balance = 20)),
        trackingMonth(FEB, food(budgeted = 100, spent = -100, balance = 0)),
      )

    assertThat(data.items[JAN])
      .isEqualTo(
        BudgetAnalysisDatum(
          budgeted = Amount(100),
          spent = Amount(-80),
          overspendingAdjustment = Zero,
          balance = Amount(20),
        ),
      )
    assertThat(data.items[FEB]?.balance).isEqualTo(Zero)
  }

  @Test
  fun `Tracking budgets carry balances with carryover`() {
    val data =
      calculate(
        trackingMonth(DEC, food(budgeted = 100, spent = -60, balance = 40, carryover = true)),
        trackingMonth(JAN, food(budgeted = 100, spent = -150, balance = -10, carryover = true)),
        trackingMonth(FEB, food(budgeted = 100, spent = -50, balance = 40, carryover = true)),
      )

    assertThat(data.items[JAN]?.balance).isEqualTo(Amount(-10))
    assertThat(data.items[FEB]?.balance).isEqualTo(Amount(40))
    assertThat(data.totalOverspendingAdjustment).isEqualTo(Zero)
  }

  @Test
  fun `Tracking budgets reset overspending without an adjustment`() {
    val data =
      calculate(
        trackingMonth(DEC),
        trackingMonth(JAN, food(budgeted = 100, spent = -150, balance = -50)),
        trackingMonth(FEB, food(budgeted = 100, spent = -100, balance = 0)),
      )

    assertThat(data.items[JAN]?.balance).isEqualTo(Amount(-50))
    assertThat(data.items[FEB]?.overspendingAdjustment).isEqualTo(Zero)
    assertThat(data.items[FEB]?.balance).isEqualTo(Zero)
    assertThat(data.totalOverspendingAdjustment).isEqualTo(Zero)
  }

  private fun calculate(vararg months: BudgetMonth, params: BudgetAnalysisParams = PARAMS) =
    calculateBudgetAnalysis(months.toList(), params)

  private fun month(month: YearMonth, vararg categories: CategoryMonth) =
    BudgetMonth.Envelope(
      month = month,
      toBudget = Zero,
      budgeted = Zero,
      spent = Zero,
      balance = Zero,
      income = Zero,
      fromLastMonth = Zero,
      lastMonthOverspent = Zero,
      buffered = Zero,
      categories = categories.toList().toImmutableList(),
    )

  private fun trackingMonth(month: YearMonth, vararg categories: CategoryMonth) =
    BudgetMonth.Tracking(
      month = month,
      budgeted = Zero,
      spent = Zero,
      balance = Zero,
      income = Zero,
      incomeBudgeted = Zero,
      categories = categories.toList().toImmutableList(),
    )

  private fun food(
    budgeted: Long = 0,
    spent: Long = 0,
    balance: Long = 0,
    carryover: Boolean = false,
  ) = category(FOOD, budgeted, spent, balance, carryover = carryover)

  private fun category(
    id: CategoryId,
    budgeted: Long,
    spent: Long,
    balance: Long,
    carryover: Boolean = false,
    isIncome: Boolean = false,
    isHidden: Boolean = false,
  ) =
    CategoryMonth(
      id = id,
      name = id.value,
      group = GROUP,
      isIncome = isIncome,
      isHidden = isHidden,
      budgeted = Amount(budgeted),
      spent = Amount(spent),
      balance = Amount(balance),
      carryover = carryover,
    )

  private companion object {
    val DEC = YearMonth(2025, 12)
    val JAN = YearMonth(2026, JANUARY)
    val FEB = YearMonth(2026, FEBRUARY)
    val MAR = YearMonth(2026, MARCH)

    val FOOD = CategoryId("food")
    val RENT = CategoryId("rent")
    val INCOME = CategoryId("income")
    val HIDDEN = CategoryId("hidden")
    val GROUP = CategoryGroupId("group")

    val PARAMS =
      BudgetAnalysisParams(
        title = null,
        conditions = emptyList(),
        conditionsOp = And,
        showHiddenCategories = false,
        graphType = Bar,
        showBalance = true,
        balanceOnly = false,
      )
  }
}
