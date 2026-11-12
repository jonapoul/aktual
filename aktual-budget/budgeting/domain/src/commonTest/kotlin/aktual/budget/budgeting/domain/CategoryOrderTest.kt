package aktual.budget.budgeting.domain

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.buildDatabase
import aktual.budget.db.dao.BudgetDao
import aktual.budget.db.dao.DatabaseTables.CATEGORIES
import aktual.budget.db.dao.DatabaseTables.CATEGORY_GROUPS
import aktual.budget.db.dao.SyncDao
import aktual.budget.model.BudgetId
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.budget.model.LocalChange
import aktual.budget.model.localChange
import aktual.budget.utils.BudgetSyncController
import aktual.test.inMemoryDriverFactory
import alakazam.test.TestCoroutineContexts
import app.cash.sqldelight.db.SqlDriver
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsOnly
import assertk.assertions.isEmpty
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.time.Clock
import kotlinx.coroutines.test.runTest

internal class CategoryOrderTest {
  @Test
  fun `Items already in order keep their sort orders`() {
    assertThat(sortOrders(listOf("a" to 1.0, "b" to 2.0, "c" to 3.0))).isEmpty()
  }

  @Test
  fun `A moved item goes between its new neighbours`() {
    val orders = sortOrders(listOf("a" to 100.0, "c" to 300.0, "b" to 200.0, "d" to 400.0))
    assertThat(orders).containsOnly("b" to 350.0)
  }

  @Test
  fun `An item moved to the top goes below the first kept one`() {
    val orders = sortOrders(listOf("c" to 300.0, "a" to 100.0, "b" to 200.0))
    assertThat(orders).containsOnly("c" to 50.0)
  }

  @Test
  fun `An item from another group with no stored order is placed between its neighbours`() {
    val orders = sortOrders(listOf("a" to 100.0, "new" to null, "b" to 200.0))
    assertThat(orders).containsOnly("new" to 150.0)
  }

  @Test
  fun `An item moved to the end goes one increment after the last`() {
    val orders = sortOrders(listOf("a" to 100.0, "b" to null))
    assertThat(orders).containsOnly("b" to 100.0 + SORT_INCREMENT)
  }

  @Test
  fun `Everything is renumbered when there's no room`() {
    val orders = sortOrders(listOf("a" to 1.0, "new" to null, "b" to 1.5))
    assertThat(orders)
      .containsOnly("a" to SORT_INCREMENT, "new" to SORT_INCREMENT * 2, "b" to SORT_INCREMENT * 3)
  }

  @Test
  fun `Groups load in budget order with their categories`() = runOrderTest {
    assertThat(order.load())
      .containsExactly(
        group(GROUP1, "Group 1", CAT1 to "Cat 1", CAT2 to "Cat 2"),
        group(GROUP2, "Group 2", CAT3 to "Cat 3"),
        group(INCOME_GROUP, "Income", INCOME to "Salary", isIncome = true),
      )
  }

  @Test
  fun `Saving the loaded order sends nothing`() = runOrderTest {
    order.save(order.load())

    assertThat(syncCalls).isEmpty()
  }

  @Test
  fun `Reordering within a group writes only the moved category`() = runOrderTest {
    val groups = order.load()
    val group1 = groups[0]
    order.save(
      listOf(group1.copy(categories = group1.categories.reversed())) + groups.drop(1),
    )

    assertThat(syncCalls.single())
      .containsExactly(localChange(CATEGORIES, "cat1", "sort_order", 49152.0))
  }

  @Test
  fun `Moving a category to another group writes its group and sort order`() = runOrderTest {
    val (group1, group2, income) = order.load()
    val moved = group1.categories.first()
    order.save(
      listOf(
        group1.copy(categories = group1.categories.drop(1)),
        group2.copy(categories = group2.categories + moved),
        income,
      ),
    )

    assertThat(syncCalls.single())
      .containsExactly(
        localChange(CATEGORIES, "cat1", "cat_group", "group2"),
        localChange(CATEGORIES, "cat1", "sort_order", 16384.0 + SORT_INCREMENT),
      )
  }

  @Test
  fun `Reordering groups writes the moved group`() = runOrderTest {
    val (group1, group2, income) = order.load()
    order.save(listOf(group2, group1, income))

    assertThat(syncCalls.single())
      .containsExactly(localChange(CATEGORY_GROUPS, "group1", "sort_order", 49152.0))
  }

  private fun group(
    id: CategoryGroupId,
    name: String,
    vararg categories: Pair<CategoryId, String>,
    isIncome: Boolean = false,
  ) =
    OrderedGroup(
      id = id,
      name = name,
      isIncome = isIncome,
      isHidden = false,
      categories =
        categories.map { (category, label) -> OrderedCategory(category, label, isHidden = false) },
    )

  private class OrderTestScope(database: BudgetDatabase, private val syncDao: SyncDao) :
    BudgetSyncController {
    val syncCalls = mutableListOf<List<LocalChange>>()
    val order =
      CategoryOrderImpl(
        budgetDao = BudgetDao(database, TestCoroutineContexts(EmptyCoroutineContext)),
        syncController = this,
      )

    override suspend fun syncChanges(changes: List<LocalChange>) {
      syncCalls.add(changes)
      syncDao.sendMessages(changes)
    }

    override fun schedule() = Unit
  }

  private fun runOrderTest(action: suspend OrderTestScope.() -> Unit) = runTest {
    val driver = inMemoryDriverFactory().create(BudgetId("abc-123"))
    driver.use {
      val database = buildDatabase(driver)
      SETUP.forEach { sql -> driver.run(sql) }
      OrderTestScope(database, SyncDao(database, driver, Clock.System)).action()
    }
  }

  private fun SqlDriver.run(sql: String) = execute(null, sql, 0)

  private companion object {
    const val SORT_INCREMENT = 16384.0

    val GROUP1 = CategoryGroupId("group1")
    val GROUP2 = CategoryGroupId("group2")
    val INCOME_GROUP = CategoryGroupId("income-group")
    val CAT1 = CategoryId("cat1")
    val CAT2 = CategoryId("cat2")
    val CAT3 = CategoryId("cat3")
    val INCOME = CategoryId("income-cat")

    @Suppress("MaxLineLength")
    val SETUP =
      listOf(
        "INSERT INTO category_groups(id, name, is_income, sort_order) VALUES ('income-group', 'Income', 1, 16384)",
        "INSERT INTO category_groups(id, name, is_income, sort_order) VALUES ('group1', 'Group 1', 0, 16384)",
        "INSERT INTO category_groups(id, name, is_income, sort_order) VALUES ('group2', 'Group 2', 0, 32768)",
        "INSERT INTO categories(id, name, is_income, cat_group, sort_order) VALUES ('income-cat', 'Salary', 1, 'income-group', 16384)",
        "INSERT INTO categories(id, name, is_income, cat_group, sort_order) VALUES ('cat1', 'Cat 1', 0, 'group1', 16384)",
        "INSERT INTO categories(id, name, is_income, cat_group, sort_order) VALUES ('cat2', 'Cat 2', 0, 'group1', 32768)",
        "INSERT INTO categories(id, name, is_income, cat_group, sort_order) VALUES ('cat3', 'Cat 3', 0, 'group2', 16384)",
      )
  }
}
