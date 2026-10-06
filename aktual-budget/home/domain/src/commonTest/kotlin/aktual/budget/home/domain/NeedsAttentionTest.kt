package aktual.budget.home.domain

import aktual.budget.budgeting.domain.BudgetMonth
import aktual.budget.budgeting.domain.CategoryMonth
import aktual.budget.model.Amount
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import kotlin.test.Test
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.YearMonth

internal class NeedsAttentionTest {
  @Test
  fun `Envelope overspending skips income, rollover and covered categories`() {
    val categories =
      persistentListOf(
        category("food", balance = -2_000),
        category("fuel", balance = 0),
        category("rent", balance = 500),
        category("debt", balance = -9_000, carryover = true),
        category("salary", balance = -100, isIncome = true),
      )

    assertThat(envelope(categories).overspentCategories())
      .containsExactly(OverspentCategory(CategoryId("food"), "food", Amount(-2_000L)))
  }

  @Test
  fun `Tracking budgets have no overspent categories`() {
    val budget =
      BudgetMonth.Tracking(
        month = MONTH,
        budgeted = Zero,
        spent = Zero,
        balance = Zero,
        income = Zero,
        incomeBudgeted = Zero,
        categories = persistentListOf(category("food", balance = -2_000)),
      )

    assertThat(budget.overspentCategories()).isEmpty()
  }

  private fun envelope(categories: ImmutableList<CategoryMonth>) =
    BudgetMonth.Envelope(
      month = MONTH,
      toBudget = Zero,
      budgeted = Zero,
      spent = Zero,
      balance = Zero,
      income = Zero,
      fromLastMonth = Zero,
      lastMonthOverspent = Zero,
      buffered = Zero,
      categories = categories,
    )

  private fun category(
    id: String,
    balance: Long,
    carryover: Boolean = false,
    isIncome: Boolean = false,
  ) =
    CategoryMonth(
      id = CategoryId(id),
      name = id,
      group = CategoryGroupId("group"),
      isIncome = isIncome,
      isHidden = false,
      budgeted = Zero,
      spent = Zero,
      balance = Amount(balance),
      carryover = carryover,
    )

  private companion object {
    val MONTH = YearMonth(2026, 4)
  }
}
