package aktual.budget.db.dao

import aktual.budget.db.model.Preference
import aktual.budget.model.AccountId
import aktual.budget.model.SyncedPrefKey
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.runDatabaseTest
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle

internal class PreferencesDaoTest {
  @Test
  fun `Getting from empty table returns null`() = runDaoTest {
    assertThat(getValue(SyncedPrefKey.Global.DateFormat)).isNull()
  }

  @Test
  fun `Observing by flow`() = runDaoTest { scope ->
    val key = SyncedPrefKey.Global.BudgetType
    observe(key).test {
      assertThatNextEmissionIsEqualTo(null)
      setValue(Preference(key, "value1"))
      assertThatNextEmissionIsEqualTo("value1")
      setValue(Preference(key, "value2"))
      assertThatNextEmissionIsEqualTo("value2")

      setValue(Preference(SyncedPrefKey.Other("some-other-key"), "whatever"))
      scope.advanceUntilIdle()
      expectNoEvents()

      setValue(Preference(key, null))
      assertThatNextEmissionIsEqualTo(null)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Get all`() = runDaoTest {
    assertAllPreferences()

    val id = AccountId("abc-123")
    val key1 = SyncedPrefKey.Global.LearnCategories
    val key2 = SyncedPrefKey.PerAccount.CsvMappings(id)

    setValue(Preference(key1, "value1"))
    assertAllPreferences(key1 to "value1")

    setValue(Preference(key2, "value2"))
    assertAllPreferences(key1 to "value1", key2 to "value2")

    setValue(Preference(key2, "value2.1"))
    assertAllPreferences(key1 to "value1", key2 to "value2.1")
  }

  private fun runDaoTest(action: suspend PreferencesDao.(TestScope) -> Unit) = runDatabaseTest {
    val db = buildDatabase()
    val dao = db.preferences()
    action(dao, scope)
  }

  private suspend fun PreferencesDao.assertAllPreferences(
    vararg expected: Pair<SyncedPrefKey, String?>
  ) = assertThat(getAll()).isEqualTo(expected.toMap())
}
