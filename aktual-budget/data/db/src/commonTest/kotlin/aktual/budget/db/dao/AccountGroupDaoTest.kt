package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.test.run
import aktual.budget.db.test.runDriverTest
import aktual.budget.model.AccountGroupId
import aktual.test.assertThatNextEmissionIsEqualTo
import app.cash.sqldelight.db.SqlDriver
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlinx.coroutines.flow.map

internal class AccountGroupDaoTest {
  private lateinit var driver: SqlDriver

  @Test
  fun `Live groups are in sort order, then ID`() = runDaoTest { groups ->
    // given
    insertGroup("b", sortOrder = 2.0)
    insertGroup("c", sortOrder = 1.0)
    insertGroup("a", sortOrder = 2.0)
    insertGroup("d", sortOrder = 0.5, tombstone = true)

    // then
    assertThat(groups.getAllActive().map { it.id })
      .containsExactly(AccountGroupId("c"), AccountGroupId("a"), AccountGroupId("b"))
  }

  @Test
  fun `Observed groups re-emit when one is added`() = runDaoTest { groups ->
    // given
    insertGroup("a", sortOrder = 1.0)

    groups.observeAllActive().map { rows -> rows.map { it.id.value } }.test {
      assertThatNextEmissionIsEqualTo(listOf("a"))

      // when
      insertGroup("b", sortOrder = 2.0)
      driver.notifyListeners("account_groups")

      // then
      assertThatNextEmissionIsEqualTo(listOf("a", "b"))
    }
  }

  @Test
  fun `Max sort order is null without live groups`() = runDaoTest { groups ->
    // given
    insertGroup("a", sortOrder = 5.0, tombstone = true)

    // then
    assertThat(groups.maxSortOrder()).isNull()
  }

  @Test
  fun `Max sort order skips deleted groups`() = runDaoTest { groups ->
    // given
    insertGroup("a", sortOrder = 2.0)
    insertGroup("b", sortOrder = 5.0, tombstone = true)

    // then
    assertThat(groups.maxSortOrder()).isEqualTo(2.0)
  }

  private suspend fun insertGroup(id: String, sortOrder: Double, tombstone: Boolean = false) {
    val dead = if (tombstone) 1 else 0
    driver.run(
      "INSERT INTO account_groups (id, name, sort_order, tombstone) " +
        "VALUES ('$id', '$id', $sortOrder, $dead)"
    )
  }

  private fun runDaoTest(action: suspend BudgetDatabase.(AccountGroupDao) -> Unit) =
    runDriverTest { driver, _ ->
      this@AccountGroupDaoTest.driver = driver
      action(AccountGroupDao(this))
    }
}
