package aktual.budget.reports.vm

import aktual.budget.model.CategoryId
import aktual.budget.model.Condition
import aktual.budget.model.Field
import aktual.budget.model.Operator
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

class CustomReportCategoriesTest {
  @Test
  fun `No category condition selects everything`() {
    assertThat(emptyList<Condition>().selectedCategories(ALL)).isEqualTo(ALL.toSet())
  }

  @Test
  fun `Category conditions narrow the selection`() {
    assertThat(listOf(condition(OneOf, ids(FOOD.value, FUEL.value))).selectedCategories(ALL))
      .isEqualTo(setOf(FOOD, FUEL))
    assertThat(listOf(condition(NotOneOf, ids(FOOD.value))).selectedCategories(ALL))
      .isEqualTo(setOf(FUEL, RENT))
    assertThat(listOf(condition(Is, JsonPrimitive(RENT.value))).selectedCategories(ALL))
      .isEqualTo(setOf(RENT))
  }

  @Test
  fun `Narrowing the selection adds a one of condition`() {
    val notes = Condition(field = Notes, operator = Contains, value = JsonPrimitive("x"))
    assertThat(listOf(notes).withSelectedCategories(setOf(FUEL, FOOD), ALL))
      .containsExactly(notes, condition(OneOf, ids(FOOD.value, FUEL.value)))
  }

  @Test
  fun `Selecting everything removes the condition`() {
    assertThat(listOf(condition(OneOf, ids(FOOD.value))).withSelectedCategories(ALL.toSet(), ALL))
      .isEmpty()
    assertThat(emptyList<Condition>().withSelectedCategories(ALL.toSet(), ALL)).isEmpty()
  }

  @Test
  fun `Not one of conditions stay that way`() {
    assertThat(
        listOf(condition(NotOneOf, ids(FOOD.value))).withSelectedCategories(setOf(FOOD), ALL),
      )
      .containsExactly(condition(NotOneOf, ids(FUEL.value, RENT.value)))
  }

  @Test
  fun `Other category conditions become one of`() {
    assertThat(
        listOf(condition(Is, JsonPrimitive(FOOD.value))).withSelectedCategories(setOf(RENT), ALL),
      )
      .containsExactly(condition(OneOf, ids(RENT.value)))
  }

  @Test
  fun `Complex category conditions can't be shown as a selection`() {
    assertThat(listOf(condition(OneOf, ids(FOOD.value))).hasComplexCategoryCondition()).isFalse()
    assertThat(listOf(condition(Contains, JsonPrimitive("F"))).hasComplexCategoryCondition())
      .isTrue()
    assertThat(
        listOf(condition(OneOf, ids(FOOD.value)), condition(NotOneOf, ids(FUEL.value)))
          .hasComplexCategoryCondition(),
      )
      .isTrue()
    val group = Condition(field = CategoryGroup, operator = Is, value = JsonPrimitive("g"))
    assertThat(listOf(group).hasComplexCategoryCondition()).isTrue()
  }

  private fun condition(operator: Operator, value: JsonElement) =
    Condition(field = Field.Category, operator = operator, value = value, type = Id)

  private fun ids(vararg ids: String) = JsonArray(ids.map { JsonPrimitive(it) })

  private companion object {
    val FOOD = CategoryId("food")
    val FUEL = CategoryId("fuel")
    val RENT = CategoryId("rent")
    val ALL = listOf(FOOD, FUEL, RENT)
  }
}
