package aktual.budget.transactions.domain

import aktual.budget.db.dao.PreferencesDao
import aktual.budget.model.AccountId
import aktual.budget.model.MessageValue
import aktual.budget.model.SyncedPrefKey.PerAccount.HideReconciled
import aktual.test.TestSyncController
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.runDatabaseTest
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import kotlin.test.Test
import kotlinx.coroutines.test.StandardTestDispatcher

class AccountViewPrefsTest {
  @Test
  fun `Options are off unless the pref says true`() = runDatabaseTest { scope ->
    // given
    val preferences =
      PreferencesDao(this, TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler)))
    val prefs = AccountViewPrefs(preferences, TestSyncController())
    val key = HideReconciled(ACCOUNT)

    prefs.observe(key).test {
      // then
      assertThatNextEmissionIsEqualTo(false)

      // when
      preferences[key] = "true"

      // then
      assertThatNextEmissionIsEqualTo(true)

      // when
      preferences[key] = "nonsense"

      // then
      assertThatNextEmissionIsEqualTo(false)
    }
  }

  @Test
  fun `Setting an option syncs it as a string`() = runDatabaseTest { scope ->
    // given
    val sync = TestSyncController()
    val preferences =
      PreferencesDao(this, TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler)))
    val prefs = AccountViewPrefs(preferences, sync)

    // when
    prefs.set(HideReconciled(ACCOUNT), enabled = true)

    // then
    assertThat(sync.changes.map { Triple(it.row, it.column, it.value) })
      .containsExactly(Triple("hide-reconciled-a", "value", MessageValue.String("true")))
  }

  private companion object {
    val ACCOUNT = AccountId("a")
  }
}
