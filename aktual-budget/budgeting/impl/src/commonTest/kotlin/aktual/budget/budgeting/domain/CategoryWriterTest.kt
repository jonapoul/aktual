package aktual.budget.budgeting.domain

import aktual.budget.budgeting.domain.NameResult.Blank
import aktual.budget.budgeting.domain.NameResult.Duplicate
import aktual.budget.budgeting.domain.NameResult.Saved
import aktual.budget.db.buildDatabase
import aktual.budget.db.dao.BudgetDao
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.SyncDao
import aktual.budget.model.Amount
import aktual.budget.model.BudgetId
import aktual.budget.model.BudgetType
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.budget.model.LocalChange
import aktual.budget.utils.BudgetSync
import aktual.core.utils.UuidGenerator
import aktual.test.inMemoryDriverFactory
import alakazam.test.TestCoroutineContexts
import app.cash.sqldelight.db.SqlDriver
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isTrue
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.time.Clock
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.YearMonth

internal class CategoryWriterTest {
  @Test
  fun `A new category goes at the top of its group`() = runCategoryTest {
    val result = writer.createCategory(GROUP1, "  Rent  ")

    assertThat(result).isEqualTo(Saved(CategoryId("id-1")))
    assertThat(categoryNames(GROUP1)).containsExactly("Rent", "Cat 1", "Cat 2")
    assertThat(budgetDao.mappedTo(CategoryId("id-1"))).containsExactly(CategoryId("id-1"))
  }

  @Test
  fun `A new category in the income group is an income category`() = runCategoryTest {
    writer.createCategory(INCOME_GROUP, "Bonus")

    val created = budgetDao.categories().first { it.name == "Bonus" }
    assertThat(created.is_income).isEqualTo(true)
  }

  @Test
  fun `A category name already in the group is a duplicate, ignoring case`() = runCategoryTest {
    assertThat(writer.createCategory(GROUP1, "cat 1")).isEqualTo(Duplicate)
    assertThat(syncCalls).isEmpty()
  }

  @Test
  fun `A category name from another group isn't a duplicate`() = runCategoryTest {
    assertThat(writer.createCategory(GROUP2, "Cat 1")).isInstanceOf<Saved<*>>()
  }

  @Test
  fun `Blank names are rejected`() = runCategoryTest {
    assertThat(writer.createCategory(GROUP1, "  ")).isEqualTo(Blank)
    assertThat(writer.createGroup("")).isEqualTo(Blank)
    assertThat(writer.renameCategory(CAT1, " ")).isEqualTo(Blank)
    assertThat(writer.renameGroup(GROUP1, " ")).isEqualTo(Blank)
    assertThat(syncCalls).isEmpty()
  }

  @Test
  fun `A new group goes after the last one`() = runCategoryTest {
    val result = writer.createGroup("Bills")

    assertThat(result).isEqualTo(Saved(CategoryGroupId("id-1")))
    assertThat(budgetDao.categoryGroups().filter { it.is_income != true }.map { it.name })
      .containsExactly("Group 1", "Group 2", "Bills")
  }

  @Test
  fun `A group name already used is a duplicate`() = runCategoryTest {
    assertThat(writer.createGroup("income")).isEqualTo(Duplicate)
  }

  @Test
  fun `Renaming a category trims the name`() = runCategoryTest {
    assertThat(writer.renameCategory(CAT1, " Food ")).isEqualTo(Saved(Unit))
    assertThat(categoryNames(GROUP1)).containsExactly("Food", "Cat 2")
  }

  @Test
  fun `Renaming a category to a sibling's name is a duplicate`() = runCategoryTest {
    assertThat(writer.renameCategory(CAT1, "CAT 2")).isEqualTo(Duplicate)
  }

  @Test
  fun `Changing only a name's case isn't a duplicate`() = runCategoryTest {
    assertThat(writer.renameCategory(CAT1, "CAT 1")).isEqualTo(Saved(Unit))
    assertThat(writer.renameGroup(GROUP1, "GROUP 1")).isEqualTo(Saved(Unit))
  }

  @Test
  fun `Keeping the same name sends nothing`() = runCategoryTest {
    writer.renameCategory(CAT1, "Cat 1")
    writer.renameGroup(GROUP1, "Group 1")

    assertThat(syncCalls).isEmpty()
  }

  @Test
  fun `Renaming a group to another's name is a duplicate`() = runCategoryTest {
    assertThat(writer.renameGroup(GROUP1, "group 2")).isEqualTo(Duplicate)
  }

  @Test
  fun `Hiding a group can be undone`() = runCategoryTest {
    val token = writer.setGroupHidden(GROUP1, hidden = true)
    assertThat(budgetDao.categoryGroups().first { it.id == GROUP1 }.hidden).isTrue()

    invoke(token.changes)
    assertThat(budgetDao.categoryGroups().first { it.id == GROUP1 }.hidden).isFalse()
  }

  @Test
  fun `An unused category needs no transfer`() = runCategoryTest {
    assertThat(writer.isTransferRequired(listOf(CAT1))).isFalse()
  }

  @Test
  fun `A category with transactions needs a transfer`() = runCategoryTest {
    insertTransaction(CAT1)

    assertThat(writer.isTransferRequired(listOf(CAT1))).isTrue()
  }

  @Test
  fun `A category with a budgeted amount needs a transfer`() = runCategoryTest {
    insertBudget(JAN, CAT1, 100)

    assertThat(writer.isTransferRequired(listOf(CAT1))).isTrue()
  }

  @Test
  fun `Only budgets of the current budget type count`() = runCategoryTest {
    run("INSERT INTO reflect_budgets(id, month, category, amount) VALUES ('r', 202401, 'cat1', 5)")

    assertThat(writer.isTransferRequired(listOf(CAT1))).isFalse()
  }

  @Test
  fun `Deleting a category without a transfer tombstones it`() = runCategoryTest {
    writer.deleteCategory(CAT1, transferTo = null)

    assertThat(categoryNames(GROUP1)).containsExactly("Cat 2")
  }

  @Test
  fun `Deleting a category moves its budgets and mappings`() = runCategoryTest {
    run("INSERT INTO category_mapping(id, transferId) VALUES ('old', 'cat1')")
    insertBudget(JAN, CAT1, 100)
    insertBudget(FEB, CAT1, 50)
    insertBudget(JAN, CAT2, 30)

    writer.deleteCategory(CAT1, transferTo = CAT2)

    assertThat(budgeted(JAN, CAT2)).isEqualTo(Amount(130))
    assertThat(budgeted(FEB, CAT2)).isEqualTo(Amount(50))
    assertThat(budgetDao.mappedTo(CAT2)).containsExactlyInAnyOrder(CAT2, CAT1, CategoryId("old"))
    assertThat(budgetDao.mappedTo(CAT1)).isEmpty()
  }

  @Test
  fun `Undoing a delete brings everything back`() = runCategoryTest {
    insertBudget(JAN, CAT1, 100)
    insertBudget(FEB, CAT1, 50)
    insertBudget(JAN, CAT2, 30)

    val token = writer.deleteCategory(CAT1, transferTo = CAT2)
    invoke(token.changes)

    assertThat(categoryNames(GROUP1)).containsExactly("Cat 1", "Cat 2")
    assertThat(budgeted(JAN, CAT2)).isEqualTo(Amount(30))
    assertThat(budgeted(FEB, CAT2)).isEqualTo(Amount(0))
    assertThat(budgetDao.mappedTo(CAT1)).containsExactly(CAT1)
  }

  @Test
  fun `Income categories keep their budgets when deleted`() = runCategoryTest {
    run(
      "INSERT INTO categories(id, name, is_income, cat_group, sort_order) " +
        "VALUES ('income2', 'Other', 1, 'income-group', 2)",
    )
    insertBudget(JAN, INCOME, 100)

    writer.deleteCategory(INCOME, transferTo = CategoryId("income2"))

    assertThat(budgetDao.budgetRow(BudgetType.Envelope, JAN, CategoryId("income2"))).isNull()
  }

  @Test
  fun `Transferring between income and expense fails`() = runCategoryTest {
    assertFailure { writer.deleteCategory(CAT1, transferTo = INCOME) }
    assertThat(syncCalls).isEmpty()
  }

  @Test
  fun `Deleting a group deletes its categories and moves their budgets`() = runCategoryTest {
    insertBudget(JAN, CAT1, 100)
    insertBudget(JAN, CAT2, 20)

    val token = writer.deleteGroup(GROUP1, transferTo = CAT3)

    assertThat(budgetDao.categoryGroups().map { it.id }).containsExactly(GROUP2, INCOME_GROUP)
    assertThat(budgeted(JAN, CAT3)).isEqualTo(Amount(120))

    invoke(token.changes)
    assertThat(categoryNames(GROUP1)).containsExactly("Cat 1", "Cat 2")
  }

  private class CategoryTestScope(
    private val driver: SqlDriver,
    private val syncDao: SyncDao,
    val budgetDao: BudgetDao,
    preferencesDao: PreferencesDao,
  ) : BudgetSync {
    val syncCalls = mutableListOf<List<LocalChange>>()
    private var nextId = 1
    val writer =
      CategoryWriterImpl(
        budgetDao = budgetDao,
        preferencesDao = preferencesDao,
        budgetSync = this,
        uuidGenerator = UuidGenerator { "id-${nextId++}" },
      )

    override suspend fun invoke(changes: List<LocalChange>) {
      syncCalls.add(changes)
      syncDao.sendMessages(changes)
    }

    override fun schedule() = Unit

    suspend fun categoryNames(group: CategoryGroupId): List<String?> =
      budgetDao.categoryOrder().filter { it.group_id == group }.map { it.name }

    suspend fun budgeted(month: YearMonth, category: CategoryId): Amount? =
      budgetDao.budgetRow(BudgetType.Envelope, month, category)?.amount

    suspend fun insertBudget(month: YearMonth, category: CategoryId, amount: Int) =
      run(
        "INSERT INTO zero_budgets(id, month, category, amount) VALUES " +
          "('${month.dbMonth}-${category.value}', ${month.dbMonth}, '${category.value}', $amount)",
      )

    suspend fun insertTransaction(category: CategoryId) =
      run(
        "INSERT INTO transactions(id, acct, category, amount, date, tombstone) " +
          "VALUES ('t1', 'account1', '${category.value}', -100, 20240115, 0)",
      )

    suspend fun run(sql: String) {
      driver.execute(identifier = null, sql = sql, parameters = 0).await()
    }
  }

  private fun runCategoryTest(action: suspend CategoryTestScope.() -> Unit) = runTest {
    val driver = inMemoryDriverFactory().create(BudgetId("abc-123"))
    driver.use {
      val database = buildDatabase(driver)
      val contexts = TestCoroutineContexts(EmptyCoroutineContext)
      val preferences = PreferencesDao(database, contexts)
      val scope =
        CategoryTestScope(
          driver = driver,
          syncDao = SyncDao(database, driver, Clock.System),
          budgetDao = BudgetDao(database, contexts),
          preferencesDao = preferences,
        )
      SETUP.forEach { sql -> scope.run(sql) }
      scope.action()
    }
  }

  private companion object {
    val JAN = YearMonth(2024, 1)
    val FEB = YearMonth(2024, 2)

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
        "INSERT INTO categories(id, name, is_income, cat_group, sort_order) VALUES ('income-cat', 'Income', 1, 'income-group', 1)",
        "INSERT INTO categories(id, name, is_income, cat_group, sort_order) VALUES ('cat1', 'Cat 1', 0, 'group1', 16384)",
        "INSERT INTO categories(id, name, is_income, cat_group, sort_order) VALUES ('cat2', 'Cat 2', 0, 'group1', 32768)",
        "INSERT INTO categories(id, name, is_income, cat_group, sort_order) VALUES ('cat3', 'Cat 3', 0, 'group2', 16384)",
        "INSERT INTO category_mapping(id, transferId) VALUES ('income-cat', 'income-cat'), ('cat1', 'cat1'), ('cat2', 'cat2'), ('cat3', 'cat3')",
        "INSERT INTO accounts(id, name, offbudget) VALUES ('account1', 'Account 1', 0)",
      )
  }
}
