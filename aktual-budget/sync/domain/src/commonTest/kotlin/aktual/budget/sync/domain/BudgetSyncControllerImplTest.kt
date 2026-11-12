package aktual.budget.sync.domain

import aktual.budget.model.DbMetadata
import aktual.budget.utils.BudgetLocalPreferences
import aktual.budget.utils.SyncEvents
import aktual.budget.utils.SyncStateHolder
import aktual.core.model.ForegroundStateHolder
import aktual.di.BudgetCoroutineScope
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

class BudgetSyncControllerImplTest {
  private val syncEvents = SyncEvents()
  private val foregroundState = ForegroundStateHolder()

  @Test
  fun `Syncs again when the app comes back to the foreground`() = runTest {
    val controller = controller()

    syncEvents.completed.test {
      controller.initialize()
      assertThat(awaitItem()).isEqualTo(Succeeded)

      foregroundState.update { false }
      runCurrent()
      expectNoEvents()

      foregroundState.update { true }
      runCurrent()
      assertThat(awaitItem()).isEqualTo(Succeeded)
    }
    controller.close()
  }

  // A local-only budget, so each sync completes without reaching the server
  private fun TestScope.controller() =
    BudgetSyncControllerImpl(
      syncStateHolder = SyncStateHolder(),
      syncer = mockk(),
      scope = BudgetCoroutineScope(backgroundScope),
      prefs = mockk(),
      syncDao = mockk(relaxed = true),
      budgetMetadata = TestMetadata(DbMetadata()),
      syncEvents = syncEvents,
      foregroundState = foregroundState,
    )

  @OptIn(ExperimentalForInheritanceCoroutinesApi::class)
  private class TestMetadata(initial: DbMetadata) :
    BudgetLocalPreferences, MutableStateFlow<DbMetadata> by MutableStateFlow(initial)
}
