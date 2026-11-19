package aktual.prefs.budget

import aktual.budget.db.dao.PreferencesDao
import aktual.budget.model.MessageValue
import aktual.budget.model.SyncedPrefKey.FeatureFlag
import aktual.budget.model.SyncedPrefKey.FeatureFlag.FormulaMode
import aktual.budget.model.SyncedPrefKey.FeatureFlag.SankeyReport
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

class BudgetFeatureFlagsImplTest {
  @Test
  fun `Unset flags are off`() = runFlagsTest { flags, _, _ ->
    FeatureFlag.entries.forEach { flag -> assertThat(flags[flag].get()).isEqualTo(false) }
  }

  @Test
  fun `Reads upstream's stored values`() = runFlagsTest { flags, dao, _ ->
    dao[SankeyReport] = "true"
    assertThat(flags[SankeyReport].get()).isEqualTo(true)
  }

  @Test
  fun `Observes changes from sync`() = runFlagsTest { flags, dao, _ ->
    flags[FormulaMode].asFlow().test {
      assertThatNextEmissionIsEqualTo(false)
      dao[FormulaMode] = "true"
      assertThatNextEmissionIsEqualTo(true)
    }
  }

  @Test
  fun `Setting a flag syncs it in upstream's format`() = runFlagsTest { flags, _, sync ->
    flags[CurrencySupport].set(true)

    assertThat(sync.changes.map { it.row to it.value })
      .containsExactly("flags.currency" to MessageValue.String("true"))
  }

  private fun runFlagsTest(
    action: suspend (BudgetFeatureFlagsImpl, PreferencesDao, TestBudgetSync) -> Unit,
  ) = runDatabaseTest { scope ->
    val dao =
      PreferencesDao(this, TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler)))
    val sync = TestBudgetSync()
    action(BudgetFeatureFlagsImpl(dao, sync), dao, sync)
  }
}
