package aktual.budget.reports.vm

import aktual.budget.db.CustomReportBudgets
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.budget.model.Condition
import aktual.budget.model.Field
import aktual.budget.model.Operator
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import kotlin.test.Test
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

class CustomReportBudgetsTest {
  @Test
  fun `Positive budgets are assets and negative ones debts`() {
    val amounts = budgetAmounts(listOf(row("food", 500), row("fuel", -200)), emptyList(), And)
    assertThat(amounts.map { it.assets to it.debts }).containsExactly(500L to 0L, 0L to -200L)
  }

  @Test
  fun `Category conditions match ids and names`() {
    assertThat(matching(condition(Category, Is, string("food")))).containsExactly("food")
    assertThat(matching(condition(Category, IsNot, string("food")))).containsExactly("fuel", "rent")
    assertThat(matching(condition(Category, OneOf, strings("food", "rent"))))
      .containsExactly("food", "rent")
    assertThat(matching(condition(Category, NotOneOf, strings("food"))))
      .containsExactly("fuel", "rent")
    assertThat(matching(condition(Category, Contains, string("FU")))).containsExactly("fuel")
    assertThat(matching(condition(Category, DoesNotContain, string("o"))))
      .containsExactly("fuel", "rent")
    assertThat(matching(condition(Category, Matches, string("^r")))).containsExactly("rent")
  }

  @Test
  fun `Category group conditions match the group`() {
    assertThat(matching(condition(CategoryGroup, Is, string("bills")))).containsExactly("rent")
    assertThat(matching(condition(CategoryGroup, Contains, string("spend"))))
      .containsExactly("food", "fuel")
  }

  @Test
  fun `Conditions on other fields are ignored`() {
    assertThat(matching(condition(Payee, Is, string("shop"))))
      .containsExactly("food", "fuel", "rent")
  }

  @Test
  fun `Any unreadable category condition turns filtering off`() {
    val conditions =
      listOf(condition(Category, Is, string("food")), condition(Category, Is, JsonPrimitive(1)))
    assertThat(matching(*conditions.toTypedArray())).containsExactly("food", "fuel", "rent")
  }

  @Test
  fun `Saved filter references are ignored`() {
    val saved = condition(Category, Is, string("food")).copy(customName = "Saved")
    assertThat(matching(saved)).containsExactly("food", "fuel", "rent")
  }

  @Test
  fun `Or keeps rows matching any condition`() {
    val conditions =
      listOf(condition(Category, Is, string("food")), condition(Category, Is, string("rent")))
    assertThat(budgetAmounts(ROWS, conditions, Or).map { it.category?.value })
      .containsExactly("food", "rent")
  }

  @Test
  fun `Bad patterns match nothing and long ones match everything`() {
    assertThat(matching(condition(Category, Matches, string("(")))).isEmpty()
    assertThat(matching(condition(Category, Matches, string("x".repeat(257)))))
      .containsExactly("food", "fuel", "rent")
  }

  private fun matching(vararg conditions: Condition): List<String?> =
    budgetAmounts(ROWS, conditions.toList(), And).map { it.category?.value }

  private fun condition(field: Field, operator: Operator, value: JsonElement) =
    Condition(field = field, operator = operator, value = value)

  private fun string(value: String) = JsonPrimitive(value)

  private fun strings(vararg values: String) = JsonArray(values.map(::JsonPrimitive))

  private companion object {
    val ROWS =
      listOf(
        row("food", 100, group = "spending", groupName = "Spending"),
        row("fuel", 200, group = "spending", groupName = "Spending"),
        row("rent", 300, group = "bills", groupName = "Bills"),
      )

    fun row(
      category: String,
      amount: Long,
      group: String = "spending",
      groupName: String = "Spending",
    ) =
      CustomReportBudgets(
        interval_key = 202605,
        category = CategoryId(category),
        category_name = category,
        category_hidden = false,
        category_group = CategoryGroupId(group),
        category_group_name = groupName,
        category_group_hidden = false,
        amount = amount,
      )
  }
}
