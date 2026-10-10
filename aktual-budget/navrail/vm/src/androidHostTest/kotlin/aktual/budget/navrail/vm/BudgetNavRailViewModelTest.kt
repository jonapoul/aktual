package aktual.budget.navrail.vm

import aktual.api.client.BudgetSyncApi
import aktual.budget.model.BudgetId
import aktual.budget.model.DbMetadata
import aktual.budget.model.SyncResponse
import aktual.budget.model.localChange
import aktual.core.model.BudgetServer
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import aktual.test.TestBudgetLocalPreferences
import aktual.test.TestSyncController
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import okio.ByteString
import okio.IOException
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BudgetNavRailViewModelTest {
  private val prefs =
    TestBudgetLocalPreferences(DbMetadata(budgetName = "Household", cloudFileId = BUDGET_ID))
  private val sync = TestSyncController()
  private val api = TestBudgetSyncApi()

  @Test
  fun `Renaming updates the server, the local name and the sync log`() = runTest {
    val viewModel = viewModel(REMOTE)

    viewModel.headerState.test {
      assertThat(awaitItem().budgetName).isEqualTo("Household")

      viewModel.rename("Renamed")
      assertThat(awaitItem().budgetName).isEqualTo("Renamed")

      assertThat(api.renames).containsExactly(BUDGET_ID to "Renamed")
      assertThat(sync.changes).containsExactly(NAME_CHANGE)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Renaming without a server only changes the local name`() = runTest {
    val viewModel = viewModel(None)

    viewModel.headerState.test {
      assertThat(awaitItem().budgetName).isEqualTo("Household")

      viewModel.rename("Renamed")
      assertThat(awaitItem().budgetName).isEqualTo("Renamed")

      assertThat(api.renames).isEmpty()
      assertThat(sync.changes).containsExactly(NAME_CHANGE)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `The name is kept if the server rejects the rename`() = runTest {
    api.failure = IOException("Offline")
    val viewModel = viewModel(REMOTE)

    viewModel.headerState.test {
      assertThat(awaitItem().budgetName).isEqualTo("Household")

      viewModel.rename("Renamed")
      expectNoEvents()

      assertThat(sync.changes).isEmpty()
      cancelAndIgnoreRemainingEvents()
    }
  }

  private fun viewModel(server: BudgetServer) =
    BudgetNavRailViewModel(
      contributors = emptySet(),
      localPreferences = prefs,
      server = server,
      syncApi = api,
      sync = sync,
    )

  private class TestBudgetSyncApi : BudgetSyncApi {
    val renames = mutableListOf<Pair<BudgetId, String>>()
    var failure: Exception? = null

    override suspend fun syncBudget(requestBody: ByteString): SyncResponse = error("Unused")

    override suspend fun renameBudget(id: BudgetId, name: String) {
      failure?.let { throw it }
      renames += id to name
    }
  }

  private companion object {
    val BUDGET_ID = BudgetId("b328186c-c919-4333-959b-04e676c1ee46")
    val REMOTE = BudgetServer.Remote(ServerUrl(Https, "test.server.com"), Token("abc-123"))
    val NAME_CHANGE =
      localChange(
        dataset = "prefs",
        row = "budgetName",
        column = "value",
        value = "Renamed",
      )
  }
}
