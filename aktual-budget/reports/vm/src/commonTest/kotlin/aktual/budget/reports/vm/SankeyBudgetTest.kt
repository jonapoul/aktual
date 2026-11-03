package aktual.budget.reports.vm

import aktual.budget.budgeting.domain.BudgetMonth
import aktual.budget.budgeting.domain.CategoryGroupMonth
import aktual.budget.budgeting.domain.CategoryMonth
import aktual.budget.model.Amount
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.budget.model.Condition
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.Month.FEBRUARY
import kotlinx.datetime.Month.JANUARY
import kotlinx.datetime.Month.MARCH
import kotlinx.datetime.YearMonth
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive

class SankeyBudgetTest {
  @Test
  fun `Income and carried over money fund budgeted categories, to budget and next month`() {
    val data =
      calculate(
        SankeyBudget(
          entries =
            [
              entry("salary", "income", isIncome = true, value = 1000),
              entry("food", "bills", value = 600),
            ],
          toBudget = 300,
          fromPreviousMonth = 100,
          lastMonthOverspent = 0,
          forNextMonth = 200,
        ),
      )

    assertThat(data.flows())
      .containsExactlyInAnyOrder(
        Flow("salary", "available_income", 1000),
        Flow("from_previous_month", "available_income", 100),
        Flow("available_income", "budgeted", 600),
        Flow("budgeted", "bills", 600),
        Flow("bills", "food", 600),
        Flow("available_income", "to_budget", 300),
        Flow("available_income", "for_next_month", 200),
      )
    assertThat(data.node("to_budget").color).isEqualTo(Positive)
    assertThat(data.node("from_previous_month").label).isEqualTo(SankeyLabel.FromMonth(DECEMBER_23))
    assertThat(data.node("for_next_month").label).isEqualTo(SankeyLabel.ForMonth(FEBRUARY_24))
    assertThat(data.node("budgeted").color).isEqualTo(Primary)
  }

  @Test
  fun `Overbudgeting and money taken out of categories flow into the budget`() {
    val data =
      calculate(
        SankeyBudget(
          entries = [entry("food", "bills", value = 500), entry("fun", "bills", value = -100)],
          toBudget = -50,
          fromPreviousMonth = 0,
          lastMonthOverspent = 0,
          forNextMonth = 0,
        ),
      )

    assertThat(data.node("to_budget").label).isEqualTo(Overbudgeted)
    assertThat(data.node("to_budget").color).isEqualTo(Negative)
    assertThat(data.node("fun").label).isEqualTo(SankeyLabel.FromCategory("fun"))
    assertThat(data.flows())
      .containsExactlyInAnyOrder(
        Flow("available_income", "budgeted", 350),
        Flow("fun", "budgeted", 100),
        Flow("to_budget", "budgeted", 50),
        Flow("budgeted", "bills", 500),
        Flow("bills", "food", 500),
      )
  }

  @Test
  fun `Layers for each mode and fix ups when one end passes the other`() {
    assertThat(sankeyLayers(Budgeted))
      .isEqualTo(
        [IncomeCategory, Account, Budget, CategoryGroup, Category],
      )
    assertThat(sankeyLayers(Spent))
      .isEqualTo(
        [IncomePayee, IncomeCategory, Account, CategoryGroup, Category],
      )
    assertThat(normalizeSankeyLayers(Budgeted, IncomePayee, Category))
      .isEqualTo(SankeyLayerRange(IncomeCategory, Category))
    assertThat(normalizeSankeyLayers(Spent, Category, Category, From))
      .isEqualTo(SankeyLayerRange(IncomePayee, Category))
    assertThat(normalizeSankeyLayers(Spent, CategoryGroup, Account, To))
      .isEqualTo(SankeyLayerRange(IncomePayee, Account))
    assertThat(sankeyLayerChoices(Spent, To, Account)).isEqualTo([CategoryGroup, Category])
  }

  @Test
  fun `Totals budgeted and received amounts over the months, keeping matching categories`() {
    val budget =
      sankeyBudget(
        months =
          [
            month(JANUARY_24, toBudget = 50, fromLastMonth = 20, overspent = -5),
            month(FEBRUARY_24, toBudget = 70, fromLastMonth = 30, overspent = -10),
          ],
        next = month(MARCH_24, toBudget = 0, fromLastMonth = 90, overspent = 0),
        conditions = [NOT_FUEL],
        op = null,
      )

    assertThat(budget)
      .isEqualTo(
        SankeyBudget(
          entries =
            [
              entry("salary", "income", isIncome = true, value = 2000),
              entry("food", "bills", value = 600),
            ],
          toBudget = 70,
          fromPreviousMonth = 20,
          lastMonthOverspent = -15,
          forNextMonth = 20,
        ),
      )
  }

  private data class Flow(val from: String, val to: String, val value: Long)

  private fun SankeyData.flows() = links.map { link ->
    Flow(nodes[link.source].key, nodes[link.target].key, link.value.toLong())
  }

  private fun SankeyData.node(key: String) = nodes.first { it.key == key }

  private fun calculate(budget: SankeyBudget) =
    calculateBudgetSankey(
      title = null,
      start = JANUARY_24,
      end = JANUARY_24,
      budget = budget,
      categoryOrder = [],
      params = SankeyParams(layerFrom = IncomeCategory, layerTo = Category),
    )

  private fun entry(id: String, groupId: String, value: Long, isIncome: Boolean = false) =
    SankeyBudgetEntry(
      categoryGroupId = groupId,
      categoryGroup = groupId,
      categoryId = id,
      category = id,
      isIncome = isIncome,
      value = value,
    )

  private fun month(month: YearMonth, toBudget: Long, fromLastMonth: Long, overspent: Long) =
    BudgetMonth.Envelope(
      month = month,
      toBudget = Amount(toBudget),
      budgeted = Zero,
      spent = Zero,
      balance = Zero,
      income = Zero,
      fromLastMonth = Amount(fromLastMonth),
      lastMonthOverspent = Amount(overspent),
      buffered = Zero,
      categories = persistentListOf(),
      groups =
        persistentListOf(
          group("income", isIncome = true, category("salary", "income", spent = 1000)),
          group(
            "bills",
            isIncome = false,
            category("food", "bills", budgeted = 300),
            category("fuel", "bills", budgeted = 100),
            category("rent", "bills"),
          ),
        ),
    )

  private fun group(id: String, isIncome: Boolean, vararg categories: CategoryMonth) =
    CategoryGroupMonth(
      id = CategoryGroupId(id),
      name = id,
      isIncome = isIncome,
      isHidden = false,
      sortOrder = null,
      budgeted = Zero,
      spent = Zero,
      balance = Zero,
      categories = categories.toList().toImmutableList(),
    )

  private fun category(id: String, group: String, budgeted: Long = 0, spent: Long = 0) =
    CategoryMonth(
      id = CategoryId(id),
      name = id,
      group = CategoryGroupId(group),
      isIncome = group == "income",
      isHidden = false,
      budgeted = Amount(budgeted),
      spent = Amount(spent),
      balance = Zero,
      carryover = false,
    )

  private companion object {
    val MARCH_24 = YearMonth(2024, MARCH)
    val NOT_FUEL =
      Condition(
        field = Category,
        operator = NotOneOf,
        value = JsonArray([JsonPrimitive("fuel")]),
      )

    val DECEMBER_23 = YearMonth(2023, 12)
    val JANUARY_24 = YearMonth(2024, JANUARY)
    val FEBRUARY_24 = YearMonth(2024, FEBRUARY)
  }
}
