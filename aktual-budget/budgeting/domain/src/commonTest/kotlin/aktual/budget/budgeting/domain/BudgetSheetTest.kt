package aktual.budget.budgeting.domain

import aktual.budget.budgeting.domain.BudgetMonth.Envelope
import aktual.budget.budgeting.domain.BudgetMonth.Tracking
import aktual.budget.db.dao.CategoryBudget
import aktual.budget.model.Amount
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import assertk.Assert
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

internal class BudgetSheetTest {
  @Test
  fun `Budgets start three months before the earliest transaction`() {
    assertThat(budgetStart(LocalDate(2026, 2, 10), today = LocalDate(2026, 4, 1)))
      .isEqualTo(YearMonth(2025, 11))
  }

  @Test
  fun `Budgets start from the current month without earlier transactions`() {
    val today = LocalDate(2026, 4, 1)
    assertThat(budgetStart(earliestTransaction = null, today)).isEqualTo(YearMonth(2026, 1))
    assertThat(budgetStart(LocalDate(2026, 9, 1), today)).isEqualTo(YearMonth(2026, 1))
  }

  @Test
  fun `Envelope month budgets income and keeps what's left in each category`() {
    assertThat(envelope().envelopeMonth(JAN, JAN)).all {
      prop(Envelope::income).isEqualTo(Amount(300_000L))
      prop(Envelope::budgeted).isEqualTo(Amount(140_000L))
      prop(Envelope::spent).isEqualTo(Amount(-135_000L))
      prop(Envelope::balance).isEqualTo(Amount(5_000L))
      prop(Envelope::toBudget).isEqualTo(Amount(160_000L))
      prop(Envelope::fromLastMonth).isEqualTo(Zero)
      prop(Envelope::availableFunds).isEqualTo(Amount(300_000L))
      balances().containsExactly("food" to 5_000L, "rent" to 0L, "salary" to 0L)
    }
  }

  @Test
  fun `Envelope month carries positive balances and to-budget forward`() {
    assertThat(envelope().envelopeMonth(JAN, FEB)).all {
      prop(Envelope::income).isEqualTo(Zero)
      prop(Envelope::fromLastMonth).isEqualTo(Amount(160_000L))
      prop(Envelope::toBudget).isEqualTo(Amount(30_000L))
      balances().containsExactly("food" to -15_000L, "rent" to 0L, "salary" to 0L)
    }
  }

  @Test
  fun `Overspending comes out of next month's to-budget`() {
    assertThat(envelope().envelopeMonth(JAN, MAR)).all {
      prop(Envelope::lastMonthOverspent).isEqualTo(Amount(-15_000L))
      prop(Envelope::toBudget).isEqualTo(Amount(15_000L))
      balances().containsExactly("food" to 0L, "rent" to 0L, "salary" to 0L)
    }
  }

  @Test
  fun `Overspending with carryover stays in the category`() {
    val data = envelope(budget(FEB, "food", amount = 30_000, carryover = true))

    assertThat(data.envelopeMonth(JAN, MAR)).all {
      prop(Envelope::lastMonthOverspent).isEqualTo(Zero)
      prop(Envelope::toBudget).isEqualTo(Amount(30_000L))
      balances().containsExactly("food" to -15_000L, "rent" to 0L, "salary" to 0L)
    }
  }

  @Test
  fun `Buffered income is held until next month`() {
    val data = envelope().copy(buffered = mapOf(JAN to Amount(100_000L)))

    assertThat(data.envelopeMonth(JAN, JAN)).all {
      prop(Envelope::buffered).isEqualTo(Amount(100_000L))
      prop(Envelope::toBudget).isEqualTo(Amount(60_000L))
    }
    assertThat(data.envelopeMonth(JAN, FEB)).all {
      prop(Envelope::fromLastMonth).isEqualTo(Amount(160_000L))
      prop(Envelope::toBudget).isEqualTo(Amount(30_000L))
    }
  }

  @Test
  fun `Held income category is buffered automatically`() {
    val data = envelope(budget(JAN, "salary", amount = 0, carryover = true))

    assertThat(data.envelopeMonth(JAN, JAN)).all {
      prop(Envelope::buffered).isEqualTo(Amount(300_000L))
      prop(Envelope::toBudget).isEqualTo(Amount(-140_000L))
    }
    assertThat(data.envelopeMonth(JAN, FEB)).prop(Envelope::toBudget).isEqualTo(Amount(30_000L))
  }

  @Test
  fun `Envelope totals include hidden categories and groups`() {
    val categories = listOf(FOOD.copy(isHidden = true), RENT.copy(isGroupHidden = true), SALARY)

    assertThat(envelope().copy(categories = categories).envelopeMonth(JAN, JAN)).all {
      prop(Envelope::budgeted).isEqualTo(Amount(140_000L))
      prop(Envelope::spent).isEqualTo(Amount(-135_000L))
      prop(Envelope::balance).isEqualTo(Amount(5_000L))
      prop(Envelope::toBudget).isEqualTo(Amount(160_000L))
    }
  }

  @Test
  fun `Month before the first budget is empty`() {
    assertThat(envelope().envelopeMonth(start = JAN, month = YearMonth(2025, 12))).all {
      prop(Envelope::month).isEqualTo(YearMonth(2025, 12))
      prop(Envelope::toBudget).isEqualTo(Zero)
      balances().containsExactly("food" to 0L, "rent" to 0L, "salary" to 0L)
    }
  }

  @Test
  fun `Tracking month compares budgeted with spent and received`() {
    assertThat(tracking().trackingMonth(JAN, JAN)).all {
      prop(Tracking::budgeted).isEqualTo(Amount(140_000L))
      prop(Tracking::spent).isEqualTo(Amount(-135_000L))
      prop(Tracking::balance).isEqualTo(Amount(5_000L))
      prop(Tracking::incomeBudgeted).isEqualTo(Amount(300_000L))
      prop(Tracking::income).isEqualTo(Amount(250_000L))
      balances()
        .containsExactly("food" to 5_000L, "rent" to 0L, "secret" to -900L, "salary" to 50_000L)
    }
  }

  @Test
  fun `Tracking balance only rolls over with carryover`() {
    val data = tracking(budget(JAN, "food", amount = 40_000, carryover = true))

    assertThat(tracking().trackingMonth(JAN, FEB))
      .balances()
      .containsExactly(
        "food" to 0L,
        "rent" to 0L,
        "secret" to 0L,
        "salary" to 0L,
      )
    assertThat(data.trackingMonth(JAN, FEB))
      .balances()
      .containsExactly(
        "food" to 5_000L,
        "rent" to 0L,
        "secret" to 0L,
        "salary" to 0L,
      )
  }

  private fun Assert<BudgetMonth>.balances() = transform { month ->
    month.categories.map { it.id.value to it.balance.toLong() }
  }

  private fun envelope(vararg overrides: CategoryBudget) =
    data(
      categories = listOf(FOOD, RENT, SALARY),
      spent =
        listOf(
          spent(JAN, "salary", 300_000),
          spent(JAN, "food", -35_000),
          spent(JAN, "rent", -100_000),
          spent(FEB, "food", -50_000),
          spent(FEB, "rent", -100_000),
        ),
      budgets =
        listOf(
          budget(JAN, "food", 40_000),
          budget(JAN, "rent", 100_000),
          budget(FEB, "food", 30_000),
          budget(FEB, "rent", 100_000),
        ) + overrides,
    )

  private fun tracking(vararg overrides: CategoryBudget) =
    data(
      categories = listOf(FOOD, RENT, SECRET, SALARY),
      spent =
        listOf(
          spent(JAN, "salary", 250_000),
          spent(JAN, "food", -35_000),
          spent(JAN, "rent", -100_000),
          spent(JAN, "secret", -900),
        ),
      budgets =
        listOf(
          budget(JAN, "food", 40_000),
          budget(JAN, "rent", 100_000),
          budget(JAN, "salary", 300_000),
        ) + overrides,
    )

  // Later budgets replace earlier ones for the same month and category
  private fun data(
    categories: List<BudgetCategory>,
    spent: List<Pair<MonthCategory, Amount>>,
    budgets: List<CategoryBudget>,
  ) =
    BudgetData(
      categories = categories,
      spent = spent.toMap(),
      budgets = budgets.associateBy { MonthCategory(it.month, it.category) },
    )

  private fun spent(month: YearMonth, category: String, amount: Long) =
    MonthCategory(month, CategoryId(category)) to Amount(amount)

  private fun budget(month: YearMonth, category: String, amount: Long, carryover: Boolean = false) =
    CategoryBudget(month, CategoryId(category), Amount(amount), carryover)

  private companion object {
    val JAN = YearMonth(2026, 1)
    val FEB = YearMonth(2026, 2)
    val MAR = YearMonth(2026, 3)

    val FOOD = category("food", group = "usual")
    val RENT = category("rent", group = "bills")
    val SECRET = category("secret", group = "bills", isHidden = true)
    val SALARY = category("salary", group = "income", isIncome = true)

    fun category(id: String, group: String, isIncome: Boolean = false, isHidden: Boolean = false) =
      BudgetCategory(
        id = CategoryId(id),
        name = id,
        group = CategoryGroupId(group),
        isIncome = isIncome,
        isHidden = isHidden,
        isGroupIncome = isIncome,
        isGroupHidden = false,
      )
  }
}
