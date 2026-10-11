package aktual.budget.transactions.vm.edit

import aktual.budget.model.CategoryId
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isSameInstanceAs
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

class CategorySearchTest {
  @Test
  fun `An empty search lists everything`() {
    assertThat(GROUPS.matching("")).isSameInstanceAs(GROUPS)
  }

  @Test
  fun `Categories match by name, ignoring case`() {
    assertThat(GROUPS.matching("RENT")).containsExactly(group("Bills", "Rent"))
  }

  @Test
  fun `Groups with no matching category are left out`() {
    assertThat(GROUPS.matching("e"))
      .containsExactly(
        group("Bills", "Rent", "Water"),
        group("Food", "Groceries", "Restaurants"),
      )
    assertThat(GROUPS.matching("gro")).containsExactly(group("Food", "Groceries"))
  }

  @Test
  fun `A group that matches by name keeps all of its categories`() {
    assertThat(GROUPS.matching("food")).containsExactly(group("Food", "Groceries", "Restaurants"))
  }

  @Test
  fun `Categories with no group are searched too`() {
    assertThat(GROUPS.matching("sav")).containsExactly(group(null, "Savings"))
  }

  @Test
  fun `Nothing is listed when nothing matches`() {
    assertThat(GROUPS.matching("xyz")).isEmpty()
  }

  private companion object {
    val GROUPS =
      persistentListOf(
        group("Bills", "Rent", "Water", "Gas"),
        group("Food", "Groceries", "Restaurants"),
        group(null, "Savings"),
      )

    fun group(name: String?, vararg categories: String) =
      CategoryGroupOptions(
        name = name,
        categories = categories.map { EntityOption(CategoryId(it), it) }.toImmutableList(),
      )
  }
}
