package aktual.budget.transactions.domain

import aktual.budget.db.dao.PreferencesDao
import aktual.budget.model.AccountId
import aktual.budget.model.MessageValue
import aktual.budget.model.SyncedPrefKey
import aktual.budget.model.SyncedPrefKey.PerAccount.HideReconciled
import aktual.budget.model.SyncedPrefKey.PerAccount.ShowBalances
import aktual.budget.model.SyncedPrefKey.PerAccount.TransactionTableColumns
import aktual.test.TestBudgetSync
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.runDatabaseTest
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.test.StandardTestDispatcher

class AccountViewPrefsTest {
  @Test
  fun `Options are off unless the pref says true`() = runDatabaseTest { scope ->
    // given
    val preferences =
      PreferencesDao(this, TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler)))
    val prefs = AccountViewPrefsImpl(preferences, TestBudgetSync())
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
    val sync = TestBudgetSync()
    val preferences =
      PreferencesDao(this, TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler)))
    val prefs = AccountViewPrefsImpl(preferences, sync)

    // when
    prefs.set(HideReconciled(ACCOUNT), enabled = true)

    // then
    assertThat(sync.changes.map { Triple(it.row, it.column, it.value) })
      .containsExactly(Triple("hide-reconciled-a", "value", MessageValue.String("true")))
  }

  @Test
  fun `The running balance falls back to show-balances with no columns saved`() = runPrefsTest {
    prefs.observeRunningBalance(ACCOUNT).test {
      assertThatNextEmissionIsEqualTo(false)

      preferences[ShowBalances(ACCOUNT)] = "true"
      assertThatNextEmissionIsEqualTo(true)
    }
  }

  @Test
  fun `The budget's columns win over show-balances`() = runPrefsTest {
    preferences[ShowBalances(ACCOUNT)] = "true"

    prefs.observeRunningBalance(ACCOUNT).test {
      assertThatNextEmissionIsEqualTo(true)

      preferences[SyncedPrefKey.Global.TransactionTableColumns] = BALANCE_HIDDEN
      assertThatNextEmissionIsEqualTo(false)
    }
  }

  @Test
  fun `The account's columns win over the budget's`() = runPrefsTest {
    preferences[SyncedPrefKey.Global.TransactionTableColumns] = BALANCE_HIDDEN

    prefs.observeRunningBalance(ACCOUNT).test {
      assertThatNextEmissionIsEqualTo(false)

      preferences[TransactionTableColumns(ACCOUNT)] = BALANCE_SHOWN
      assertThatNextEmissionIsEqualTo(true)

      // Upstream clears an account's columns with an empty string
      preferences[TransactionTableColumns(ACCOUNT)] = ""
      assertThatNextEmissionIsEqualTo(false)
    }
  }

  @Test
  fun `Setting the running balance with no columns saved sets show-balances`() = runPrefsTest {
    prefs.setRunningBalance(ACCOUNT, enabled = true)

    assertThat(sync.changes.map { Triple(it.row, it.column, it.value) })
      .containsExactly(Triple("show-balances-a", "value", MessageValue.String("true")))
  }

  @Test
  fun `Setting the running balance keeps the budget's other columns`() = runPrefsTest {
    val global =
      parseTransactionTableColumns(null).reversed().map {
        if (it.id == "notes") it.copy(hidden = true) else it
      }
    preferences[SyncedPrefKey.Global.TransactionTableColumns] = global.encode()

    prefs.setRunningBalance(ACCOUNT, enabled = true)

    val change = sync.changes.single()
    assertThat(change.row).isEqualTo("transaction-table-columns-a")
    val expected = global.map { if (it.id == "balance") it.copy(hidden = false) else it }
    assertThat(parseTransactionTableColumns((change.value as MessageValue.String).value))
      .isEqualTo(expected)
  }

  private class PrefsScope(val preferences: PreferencesDao, val sync: TestBudgetSync) {
    val prefs = AccountViewPrefsImpl(preferences, sync)
  }

  private fun runPrefsTest(action: suspend PrefsScope.() -> Unit) = runDatabaseTest { scope ->
    val preferences =
      PreferencesDao(this, TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler)))
    PrefsScope(preferences, TestBudgetSync()).action()
  }

  private companion object {
    val ACCOUNT = AccountId("a")
    const val BALANCE_HIDDEN = """[{"id":"balance","hidden":true}]"""
    const val BALANCE_SHOWN = """[{"id":"balance","hidden":false}]"""
  }
}
