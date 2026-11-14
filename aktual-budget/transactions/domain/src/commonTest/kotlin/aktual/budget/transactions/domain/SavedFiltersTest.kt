package aktual.budget.transactions.domain

import aktual.budget.db.buildDatabase
import aktual.budget.db.dao.SyncDao
import aktual.budget.db.dao.TransactionFilterDao
import aktual.budget.model.BudgetId
import aktual.budget.model.Condition
import aktual.budget.model.ConditionSet
import aktual.budget.model.LocalChange
import aktual.budget.model.TransactionFilterId
import aktual.budget.utils.BudgetSyncController
import aktual.test.inMemoryDriverFactory
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlin.time.Clock
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive

class SavedFiltersTest {
  @Test
  fun `Saved filters are created, updated and deleted`() = runSavedFiltersTest {
    filters.observe().test {
      assertThat(awaitItem()).isEmpty()

      // when
      val created = filters.create(" Groceries ", ConditionSet(listOf(PAYEE)))

      // then
      assertThat(created).isEqualTo(SaveFilterResult.Saved(ID_1))
      assertThat(awaitItem())
        .containsExactly(SavedFilter(ID_1, "Groceries", ConditionSet(listOf(PAYEE))))

      // when
      filters.update(ID_1, "Food", ConditionSet(listOf(PAYEE, NOTES), Or))

      // then
      assertThat(awaitItem())
        .containsExactly(SavedFilter(ID_1, "Food", ConditionSet(listOf(PAYEE, NOTES), Or)))

      // when
      filters.delete(ID_1)

      // then
      assertThat(awaitItem()).isEmpty()
    }
  }

  @Test
  fun `A name is required`() = runSavedFiltersTest {
    assertThat(filters.create("  ", ConditionSet(listOf(PAYEE))))
      .isEqualTo(SaveFilterResult.NameRequired)
  }

  @Test
  fun `Names are unique, except against the filter itself`() = runSavedFiltersTest {
    // given
    filters.create("Groceries", ConditionSet(listOf(PAYEE)))

    // then
    assertThat(filters.create("Groceries", ConditionSet(listOf(NOTES))))
      .isEqualTo(SaveFilterResult.NameTaken("Groceries"))
    assertThat(filters.update(ID_1, "Groceries", ConditionSet(listOf(NOTES))))
      .isEqualTo(SaveFilterResult.Saved(ID_1))
  }

  @Test
  fun `The same conditions can't be saved twice`() = runSavedFiltersTest {
    // given
    filters.create("Groceries", ConditionSet(listOf(PAYEE, NOTES)))

    // then, in any order
    assertThat(filters.create("Other", ConditionSet(listOf(NOTES, PAYEE))))
      .isEqualTo(SaveFilterResult.ConditionsTaken("Groceries"))
    assertThat(filters.create("Other", ConditionSet(listOf(NOTES, PAYEE), Or)))
      .isEqualTo(SaveFilterResult.Saved(ID_3))
  }

  @Test
  fun `List values never count as the same, as upstream compares them by reference`() =
    runSavedFiltersTest {
      // given
      filters.create("Groceries", ConditionSet(listOf(ONE_OF)))

      // then
      assertThat(filters.create("Other", ConditionSet(listOf(ONE_OF))))
        .isEqualTo(SaveFilterResult.Saved(ID_2))
    }

  private class SavedFiltersTestScope(val filters: SavedFilters)

  private fun runSavedFiltersTest(action: suspend SavedFiltersTestScope.() -> Unit) = runTest {
    val driver = inMemoryDriverFactory().create(BudgetId("abc-123"))
    driver.use {
      val database = buildDatabase(driver)
      val syncDao = SyncDao(database, driver, Clock.System)
      val sync =
        object : BudgetSyncController {
          override suspend fun syncChanges(changes: List<LocalChange>) {
            syncDao.sendMessages(changes)
          }

          override fun schedule() = Unit
        }
      var nextId = 1
      val filters = SavedFilters(TransactionFilterDao(database), sync) { "filter-${nextId++}" }
      SavedFiltersTestScope(filters).action()
    }
  }

  private companion object {
    val ID_1 = TransactionFilterId("filter-1")
    val ID_2 = TransactionFilterId("filter-2")
    val ID_3 = TransactionFilterId("filter-3")
    val PAYEE = Condition(Payee, Is, JsonPrimitive("a"))
    val NOTES = Condition(Notes, Contains, JsonPrimitive("lunch"))
    val ONE_OF = Condition(Payee, OneOf, JsonArray(listOf(JsonPrimitive("a"))))
  }
}
