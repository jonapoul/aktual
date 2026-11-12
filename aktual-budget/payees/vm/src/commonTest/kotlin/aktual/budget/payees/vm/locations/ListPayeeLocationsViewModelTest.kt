package aktual.budget.payees.vm.locations

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.DatabaseTables.PAYEE_LOCATIONS
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.PayeeId
import aktual.budget.model.PayeeLocationId
import aktual.budget.payees.vm.locations.ListPayeeLocationsState.Success
import aktual.test.TestSyncController
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.runDatabaseTest
import aktual.test.viewModelContext
import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import kotlin.time.Instant
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import kotlinx.datetime.LocalDate
import org.junit.Test

class ListPayeeLocationsViewModelTest {
  @Test
  fun `No locations shows empty`() = runDatabaseTest { scope ->
    createViewModel(scope).state.test {
      assertThatNextEmissionIsEqualTo(Loading)
      assertThatNextEmissionIsEqualTo(Empty)
    }
  }

  @Test
  fun `Lists locations by payee name, newest first`() = runDatabaseTest { scope ->
    // given
    insertPayee("tesco", "Tesco")
    insertPayee("argos", "Argos")
    insertLocation("tesco-old", "tesco", Instant.parse("2026-11-01T12:00:00Z"))
    insertLocation("tesco-new", "tesco")
    insertLocation("argos", "argos")

    // then
    createViewModel(scope).state.test {
      assertThatNextEmissionIsEqualTo(Loading)
      assertThatNextEmissionIsEqualTo(
        Success(
          persistentListOf(
            item("argos", "argos", "Argos", LocalDate(2026, 11, 7)),
            item("tesco-new", "tesco", "Tesco", LocalDate(2026, 11, 7)),
            item("tesco-old", "tesco", "Tesco", LocalDate(2026, 11, 1)),
          ),
        ),
      )
    }
  }

  @Test
  fun `Deleting a location tombstones it and offers an undo`() = runDatabaseTest { scope ->
    // given
    val sync = TestSyncController()
    val viewModel = createViewModel(scope, sync)

    viewModel.events.test {
      // when
      viewModel.delete(PayeeLocationId("loc"))

      // then
      assertThatNextEmissionIsEqualTo(PayeeLocationsEvent.Deleted(PayeeLocationId("loc")))
    }
    assertThat(sync.changes)
      .containsExactly(LocalChange(PAYEE_LOCATIONS, "loc", "tombstone", MessageValue.Number(1)))
  }

  @Test
  fun `Undoing a delete untombstones it, even after the screen's closed`() =
    runDatabaseTest { scope ->
      // given
      val sync = TestSyncController()
      val viewModel = createViewModel(scope, sync)
      viewModel.viewModelScope.cancel()

      // when
      viewModel.undoDelete(PayeeLocationId("loc"))
      scope.testScheduler.advanceUntilIdle()

      // then
      assertThat(sync.changes)
        .containsExactly(LocalChange(PAYEE_LOCATIONS, "loc", "tombstone", MessageValue.Number(0)))
    }

  private fun item(id: String, payee: String, name: String, date: LocalDate) =
    PayeeLocationItem(PayeeLocationId(id), PayeeId(payee), name, HERE, date)

  private fun BudgetDatabase.createViewModel(
    scope: TestScope,
    sync: TestSyncController = TestSyncController(),
  ) =
    ListPayeeLocationsViewModel(
      loader = loader(scope, sync),
      deleter = deleter(scope, sync),
      viewModelContext = scope.viewModelContext,
    )
}
