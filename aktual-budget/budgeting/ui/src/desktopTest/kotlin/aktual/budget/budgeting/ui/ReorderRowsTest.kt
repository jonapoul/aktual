package aktual.budget.budgeting.ui

import aktual.budget.budgeting.vm.ReorderCategory
import aktual.budget.budgeting.vm.ReorderGroup
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf

class ReorderRowsTest {
  @Test
  fun `Collapsed groups hide their categories but the dragged one`() {
    val rows = reorderRows(GROUPS, collapsed = setOf(USUAL.id), dragged = FUN.id)

    assertThat(rows.map { it.key })
      .containsExactly(
        "group-usual",
        "category-fun",
        "group-bills",
        "category-rent",
        "group-income",
        "category-salary",
      )
  }

  @Test
  fun `A category moves within its group`() {
    val moved = move(key = "category-food", to = 2)

    assertThat(moved)
      .isNotNull()
      .transform { it.categoryIds() }
      .containsExactly(listOf(FUN.id, FOOD.id), listOf(RENT.id), listOf(SALARY.id))
  }

  @Test
  fun `Moving a category past the next header puts it first in that group`() {
    val moved = move(key = "category-fun", to = 3)

    assertThat(moved)
      .isNotNull()
      .transform { it.categoryIds() }
      .containsExactly(listOf(FOOD.id), listOf(FUN.id, RENT.id), listOf(SALARY.id))
  }

  @Test
  fun `Moving a category above its header puts it last in the group before`() {
    val moved = move(key = "category-rent", to = 3)

    assertThat(moved)
      .isNotNull()
      .transform { it.categoryIds() }
      .containsExactly(listOf(FOOD.id, FUN.id, RENT.id), emptyList<CategoryId>(), listOf(SALARY.id))
  }

  @Test
  fun `A category goes after the ones hidden in a collapsed group`() {
    val moved =
      movedGroups(
        GROUPS,
        collapsed = setOf(BILLS.id),
        dragged = FUN.id,
        key = "category-fun",
        to = 3,
      )

    assertThat(moved)
      .isNotNull()
      .transform { it.categoryIds() }
      .containsExactly(listOf(FOOD.id), listOf(RENT.id, FUN.id), listOf(SALARY.id))
  }

  @Test
  fun `A category can't go above the first group`() {
    assertThat(move(key = "category-food", to = 0)).isNull()
  }

  @Test
  fun `Categories can't cross between expense and income groups`() {
    assertThat(move(key = "category-rent", to = 5)).isNull()
    assertThat(move(key = "category-salary", to = 4)).isNull()
  }

  @Test
  fun `Expense groups move among themselves`() {
    val moved = movedGroups(GROUPS, ALL, dragged = null, key = "group-bills", to = 0)

    assertThat(moved)
      .isNotNull()
      .transform { groups -> groups.map { it.id } }
      .containsExactly(BILLS.id, USUAL.id, INCOME.id)
  }

  @Test
  fun `Groups keep their categories when moved`() {
    val moved = movedGroups(GROUPS, ALL, dragged = null, key = "group-usual", to = 1)

    assertThat(moved)
      .isNotNull()
      .transform { it.first() }
      .prop(ReorderGroup::categories)
      .containsExactly(RENT)
  }

  @Test
  fun `Groups can't move below income or income above expenses`() {
    assertThat(movedGroups(GROUPS, ALL, dragged = null, key = "group-bills", to = 2)).isNull()
    assertThat(movedGroups(GROUPS, ALL, dragged = null, key = "group-income", to = 0)).isNull()
  }

  private fun move(key: String, to: Int) =
    movedGroups(GROUPS, collapsed = emptySet(), dragged = null, key = key, to = to)

  private fun List<ReorderGroup>.categoryIds() = map { group -> group.categories.map { it.id } }

  private companion object {
    val FOOD = ReorderCategory(CategoryId("food"), "Food", isHidden = false)
    val FUN = ReorderCategory(CategoryId("fun"), "Fun", isHidden = false)
    val RENT = ReorderCategory(CategoryId("rent"), "Rent", isHidden = false)
    val SALARY = ReorderCategory(CategoryId("salary"), "Salary", isHidden = false)

    val USUAL = group("usual", FOOD, FUN)
    val BILLS = group("bills", RENT)
    val INCOME = group("income", SALARY, isIncome = true)
    val GROUPS = listOf(USUAL, BILLS, INCOME)
    val ALL = GROUPS.map { it.id }.toSet()

    fun group(id: String, vararg categories: ReorderCategory, isIncome: Boolean = false) =
      ReorderGroup(
        id = CategoryGroupId(id),
        name = id,
        isIncome = isIncome,
        isHidden = false,
        categories = persistentListOf(*categories),
      )
  }
}
