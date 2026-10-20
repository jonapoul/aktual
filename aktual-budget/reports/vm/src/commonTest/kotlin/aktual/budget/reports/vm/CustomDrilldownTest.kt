package aktual.budget.reports.vm

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.budget.model.Condition
import aktual.budget.rules.domain.ConditionsMatcher
import aktual.budget.rules.domain.FilterRow
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonPrimitive

class CustomDrilldownTest {
  @Test
  fun `A category's interval matches its spending in that month`() {
    val matcher = matcher(CustomDrilldown(group = FOOD_GROUP, interval = FEB))

    assertThat(matcher.matches(row())).isTrue()
    assertThat(matcher.matches(row(date = LocalDate(2026, 2, 28)))).isTrue()
    assertThat(matcher.matches(row(date = LocalDate(2026, 3, 1)))).isFalse()
    assertThat(matcher.matches(row(category = FUEL))).isFalse()
    assertThat(matcher.matches(row(amount = 500))).isFalse()
  }

  @Test
  fun `A category group matches each of its categories`() {
    val matcher = matcher(CustomDrilldown(group = SPENDING_GROUP))

    assertThat(matcher.matches(row())).isTrue()
    assertThat(matcher.matches(row(category = FUEL))).isTrue()
    assertThat(matcher.matches(row(category = SALARY, group = INCOME))).isFalse()
  }

  @Test
  fun `The whole report covers every month it sums`() {
    val matcher = matcher(CustomDrilldown())

    assertThat(matcher.matches(row(date = LocalDate(2026, 1, 1)))).isTrue()
    assertThat(matcher.matches(row(date = LocalDate(2026, 3, 31)))).isTrue()
    assertThat(matcher.matches(row(date = LocalDate(2025, 12, 31)))).isFalse()
    assertThat(matcher.matches(row(date = LocalDate(2026, 4, 1)))).isFalse()
  }

  @Test
  fun `Uncategorised and transfers are told apart`() {
    val uncategorised = matcher(CustomDrilldown(group = Uncategorized))
    val transfers = matcher(CustomDrilldown(group = Transfers))
    val plain = row(category = null, group = null)
    val transfer = plain.copy(isTransfer = true)

    assertThat(uncategorised.matches(plain)).isTrue()
    assertThat(uncategorised.matches(transfer)).isFalse()
    assertThat(transfers.matches(transfer)).isTrue()
    assertThat(transfers.matches(plain)).isFalse()
    assertThat(uncategorised.matches(row())).isFalse()
  }

  @Test
  fun `Off budget rows only show when the report shows them`() {
    val offBudget = row(category = null, group = null).copy(offBudget = true)

    assertThat(matcher(CustomDrilldown()).matches(offBudget)).isFalse()

    val config = CONFIG.copy(showOffBudget = true)
    val matcher = matcher(CustomDrilldown(group = OffBudget), config)
    assertThat(matcher.matches(offBudget)).isTrue()
    assertThat(matcher.matches(row())).isFalse()
  }

  @Test
  fun `Hidden categories only show when the report shows them`() {
    val data = data().copy(hiddenCategories = persistentSetOf(FUEL))

    assertThat(data.matcher(CustomDrilldown()).matches(row(category = FUEL))).isFalse()
    assertThat(data.matcher(CustomDrilldown()).matches(row())).isTrue()

    val shown = data.copy(config = CONFIG.copy(showHidden = true))
    assertThat(shown.matcher(CustomDrilldown()).matches(row(category = FUEL))).isTrue()
  }

  @Test
  fun `Uncategorised rows only show when the report shows them`() {
    val uncategorised = row(category = null, group = null)

    val hidden = matcher(CustomDrilldown(), CONFIG.copy(showUncategorized = false))
    assertThat(hidden.matches(uncategorised)).isFalse()
    assertThat(hidden.matches(row())).isTrue()
  }

  @Test
  fun `Net reports match both ways, unless a column picks one`() {
    val config = CONFIG.copy(balanceType = Net)
    val deposit = row(amount = 500)

    assertThat(matcher(CustomDrilldown(), config).matches(deposit)).isTrue()
    assertThat(matcher(CustomDrilldown(), config).matches(row())).isTrue()
    assertThat(matcher(CustomDrilldown(flow = In), config).matches(deposit)).isTrue()
    assertThat(matcher(CustomDrilldown(flow = In), config).matches(row())).isFalse()
    assertThat(matcher(CustomDrilldown(flow = Out), config).matches(row())).isTrue()
  }

  @Test
  fun `The report's own conditions still apply`() {
    val notes = Condition(field = Notes, operator = Contains, value = JsonPrimitive("lunch"))
    val matcher = matcher(CustomDrilldown(), CONFIG.copy(conditions = persistentListOf(notes)))

    assertThat(matcher.matches(row().copy(notes = "lunch out"))).isTrue()
    assertThat(matcher.matches(row())).isFalse()
  }

  @Test
  fun `A tag set matches notes holding exactly its tags`() {
    val data = data().copy(tagScope = persistentListOf("trip", "work"))
    val work = CustomGroup.TagSet(persistentListOf(), persistentListOf("work"))

    val matcher = data.matcher(CustomDrilldown(group = work))
    assertThat(matcher.matches(row().copy(notes = "#work lunch"))).isTrue()
    assertThat(matcher.matches(row().copy(notes = "#work #trip"))).isFalse()

    val untagged = data.matcher(CustomDrilldown(group = Untagged))
    assertThat(untagged.matches(row().copy(notes = "#other"))).isTrue()
    assertThat(untagged.matches(row().copy(notes = "#work"))).isFalse()
  }

  @Test
  fun `Budgeted reports don't drill down`() {
    val data = data(CONFIG.copy(balanceType = Budgeted))
    assertThat(data.drilldownFilter(CustomDrilldown())).isNull()
  }

  private fun matcher(drilldown: CustomDrilldown, config: CustomReportConfig = CONFIG) =
    data(config).matcher(drilldown)

  private fun CustomData.matcher(drilldown: CustomDrilldown) =
    ConditionsMatcher(checkNotNull(drilldownFilter(drilldown)))

  private fun data(config: CustomReportConfig = CONFIG) =
    CustomData(
      title = "Report",
      config = config,
      range = LocalDate(2026, 1, 10)..LocalDate(2026, 3, 20),
      intervals = persistentListOf(),
      intervalCount = 3,
      groups = persistentListOf(),
      categoryGroups = persistentListOf(),
      totals = CustomTotals(Zero, Zero, Zero, Zero, Zero),
      trends = persistentMapOf(),
    )

  private fun row(
    date: LocalDate = LocalDate(2026, 2, 3),
    amount: Long = -500,
    category: CategoryId? = FOOD,
    group: CategoryGroupId? = SPENDING,
  ) =
    FilterRow(
      date = date,
      amount = Amount(amount),
      account = AccountId("checking"),
      category = category,
      categoryGroup = group,
    )

  private companion object {
    val SPENDING = CategoryGroupId("spending")
    val INCOME = CategoryGroupId("income")
    val FOOD = CategoryId("food")
    val FUEL = CategoryId("fuel")
    val SALARY = CategoryId("salary")

    val FOOD_GROUP = CustomGroup.Category(FOOD, "Food")
    val SPENDING_GROUP = CustomGroup.Group(SPENDING, "Spending")

    // February, in a report that starts mid January
    val FEB = CustomInterval(LocalDate(2026, 2, 1), LocalDate(2026, 2, 1)..LocalDate(2026, 2, 28))

    val CONFIG =
      checkNotNull(emptyCustomReport().toConfig())
        .copy(
          interval = Monthly,
          showUncategorized = true,
          dates = CustomReportDates.Live(Last3Months, includeCurrent = true),
        )
  }
}
