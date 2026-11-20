package aktual.prefs.vm.advanced

import aktual.budget.model.DbMetadata
import aktual.budget.model.DbMetadataKey.Companion.GroupId
import aktual.budget.model.DbMetadataKey.Companion.Id
import aktual.budget.transactions.domain.RepairResult
import aktual.budget.transactions.domain.TransactionRepairer
import aktual.test.TestBudgetLocalPreferences
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.viewModelContext
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class AdvancedSettingsViewModelTest {
  @Test
  fun `Shows the budget and sync IDs`() = runTest {
    val prefs = TestBudgetLocalPreferences(DbMetadata().set(Id, "budget-1").set(GroupId, "group-1"))
    val viewModel = viewModel(prefs = prefs)

    viewModel.state.test {
      assertThat(awaitItem()).all {
        prop(AdvancedSettingsState::budgetId).isEqualTo("budget-1")
        prop(AdvancedSettingsState::syncId).isEqualTo("group-1")
      }
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `No sync ID for a local budget`() = runTest {
    val prefs = TestBudgetLocalPreferences(DbMetadata().set(Id, "budget-1"))
    val viewModel = viewModel(prefs = prefs)

    viewModel.state.test {
      assertThat(awaitItem()).prop(AdvancedSettingsState::syncId).isNull()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Repairing shows its progress then the result`() = runTest {
    val result = CompletableDeferred<RepairResult>()
    val viewModel = viewModel(repairer = { result.await() })

    viewModel.state.test {
      assertThat(awaitItem().repair).isEqualTo(Idle)
      viewModel.repairTransactions()
      assertThat(awaitItem().repair).isEqualTo(Running)
      result.complete(RESULT)
      assertThatNextEmissionIsEqualTo(
        AdvancedSettingsState(
          budgetId = null,
          syncId = null,
          repair =
            RepairState.Done(
              blankPayees = 1,
              cleared = 2,
              deleted = 0,
              transfers = 0,
              nonParentErrors = 0,
              categorisedParents = 3,
              mismatchedSplits = persistentListOf(DATE),
            ),
        ),
      )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `A failed repair shows an error`() = runTest {
    val failure = CompletableDeferred<Unit>()
    val viewModel =
      viewModel(
        repairer = {
          failure.await()
          error("Broken")
        },
      )

    viewModel.state.test {
      skipItems(1)
      viewModel.repairTransactions()
      assertThat(awaitItem().repair).isEqualTo(Running)
      failure.complete(Unit)
      assertThat(awaitItem().repair).isEqualTo(Failed)
      cancelAndIgnoreRemainingEvents()
    }
  }

  private fun TestScope.viewModel(
    prefs: TestBudgetLocalPreferences = TestBudgetLocalPreferences(),
    repairer: TransactionRepairer = TransactionRepairer { RESULT },
  ) =
    AdvancedSettingsViewModel(
      repairer = repairer,
      localPreferences = prefs,
      viewModelContext = viewModelContext,
    )

  private companion object {
    val DATE = LocalDate(2026, 10, 1)

    val RESULT =
      RepairResult(
        blankPayees = 1,
        cleared = 2,
        deleted = 0,
        transfers = 0,
        nonParentErrors = 0,
        categorisedParents = 3,
        mismatchedSplits = listOf(DATE),
      )
  }
}
