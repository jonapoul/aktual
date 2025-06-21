package actual.budget.db.dao

import actual.budget.model.AccountId
import actual.budget.model.SyncedPrefKey
import actual.test.assertEmitted
import actual.test.runDatabaseTest
import alakazam.test.core.TestCoroutineContexts
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
internal class PreferencesDaoTest {
  @Test
  fun `Getting from empty table returns null`() = runDaoTest {
    assertNull(get(key = SyncedPrefKey.Global.DateFormat))
  }

  @Test
  fun `Observing by flow`() = runDaoTest { scope ->
    val key = SyncedPrefKey.Global.BudgetType
    observe(key).test {
      assertEmitted(null)
      set(key, "value1")
      assertEmitted("value1")
      set(key, "value2")
      assertEmitted("value2")

      set(SyncedPrefKey.Other("some-other-key"), "whatever")
      scope.advanceUntilIdle()
      expectNoEvents()

      set(key, null)
      assertEmitted(null)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Get all`() = runDaoTest {
    assertAllPreferences()

    val id = AccountId("abc-123")
    val key1 = SyncedPrefKey.Global.LearnCategories
    val key2 = SyncedPrefKey.PerAccount.CsvMappings(id)

    set(key1, "value1")
    assertAllPreferences(
      key1 to "value1",
    )

    set(key2, "value2")
    assertAllPreferences(
      key1 to "value1",
      key2 to "value2",
    )

    set(key2, "value2.1")
    assertAllPreferences(
      key1 to "value1",
      key2 to "value2.1",
    )
  }

  private fun runDaoTest(action: suspend PreferencesDao.(TestScope) -> Unit) =
    runDatabaseTest { scope ->
      val dao = PreferencesDao(this, TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler)))
      action(dao, scope)
    }

  private suspend fun PreferencesDao.assertAllPreferences(vararg expected: Pair<SyncedPrefKey, String?>) =
    assertEquals(actual = getAll(), expected = expected.toMap())
}
