package aktual.budget.schedules.vm.edit

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.RulesDao
import aktual.budget.db.dao.ScheduleDao
import aktual.budget.model.AccountId
import aktual.budget.model.PayeeId
import aktual.budget.model.ScheduleId
import aktual.budget.schedules.domain.SchedulesLoaderImpl
import aktual.core.utils.Calendar
import aktual.core.utils.UuidGenerator
import aktual.test.TestBudgetLocalPreferences
import aktual.test.TestBudgetSync
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.insertSchedule
import aktual.test.runDatabaseTest
import aktual.test.viewModelContext
import alakazam.test.TestClock
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.test.Test
import kotlin.time.Instant
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.datetime.LocalDate

class EditScheduleViewModelTest {
  @Test
  fun `Existing schedule opens editable with no changes`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", name = "Rent", payee = "Landlord", account = "Current")
    val viewModel = createViewModel(scope, ScheduleId("a"))

    viewModel.state.test {
      assertThat(awaitLoaded()).all {
        prop(EditScheduleState.Loaded::isNew).isFalse()
        prop(EditScheduleState.Loaded::hasChanges).isFalse()
        prop(EditScheduleState.Loaded::canSave).isFalse()
      }
    }
  }

  @Test
  fun `Changing a field back to its saved value clears the changes`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", name = "Rent", payee = "Landlord", account = "Current")
    val viewModel = createViewModel(scope, ScheduleId("a"))

    viewModel.state.test {
      awaitLoaded()

      viewModel.setName("Mortgage")
      assertThat(awaitLoaded()).all {
        prop(EditScheduleState.Loaded::hasChanges).isTrue()
        prop(EditScheduleState.Loaded::canSave).isTrue()
      }

      viewModel.setName("Rent")
      assertThat(awaitLoaded()).prop(EditScheduleState.Loaded::hasChanges).isFalse()
    }
  }

  @Test
  fun `Saving an existing schedule emits Saved`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", name = "Rent", payee = "Landlord", account = "Current")
    val viewModel = createViewModel(scope, ScheduleId("a"))
    viewModel.state.test { awaitLoaded() }

    viewModel.events.test {
      viewModel.setName("Mortgage")
      scope.advanceUntilIdle()
      viewModel.save()
      scope.advanceUntilIdle()

      assertThatNextEmissionIsEqualTo(Saved)
    }
  }

  @Test
  fun `New schedule needs a payee and account to save`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", name = "Rent", payee = "Landlord", account = "Current")
    insertSchedule(id = "b", name = "Power", payee = "Energy", account = "Savings")
    val viewModel = createViewModel(scope, scheduleId = null)

    viewModel.state.test {
      assertThat(awaitLoaded()).all {
        prop(EditScheduleState.Loaded::isNew).isTrue()
        prop(EditScheduleState.Loaded::canSave).isFalse()
      }

      viewModel.setPayee(PayeeId("a-payee"))
      assertThat(awaitLoaded()).prop(EditScheduleState.Loaded::canSave).isFalse()

      viewModel.setAccount(AccountId("a-account"))
      assertThat(awaitLoaded()).prop(EditScheduleState.Loaded::canSave).isTrue()
    }
  }

  @Test
  fun `Creating a schedule emits Created`() = runDatabaseTest { scope ->
    insertSchedule(id = "a", name = "Rent", payee = "Landlord", account = "Current")
    val viewModel = createViewModel(scope, scheduleId = null)
    viewModel.state.test { awaitLoaded() }

    viewModel.events.test {
      viewModel.setPayee(PayeeId("a-payee"))
      viewModel.setAccount(AccountId("a-account"))
      scope.advanceUntilIdle()
      viewModel.save()
      scope.advanceUntilIdle()

      assertThatNextEmissionIsEqualTo(Created)
    }
  }

  private suspend fun ReceiveTurbine<EditScheduleState>.awaitLoaded(): EditScheduleState.Loaded {
    var state = awaitItem()
    while (state !is Loaded) state = awaitItem()
    return state
  }

  private fun BudgetDatabase.createViewModel(
    scope: TestScope,
    scheduleId: ScheduleId?,
  ): EditScheduleViewModel {
    val dispatcher = StandardTestDispatcher(scope.testScheduler)
    var nextId = 1
    return EditScheduleViewModel(
      scheduleId = scheduleId,
      scheduleDao = ScheduleDao(this),
      rulesDao = RulesDao(this),
      payeeDao = PayeeDao(this),
      accountDao = AccountDao(this),
      loader =
        SchedulesLoaderImpl(
          scheduleDao = ScheduleDao(this),
          accountDao = AccountDao(this),
          payeeDao = PayeeDao(this),
          preferencesDao = PreferencesDao(this, TestCoroutineContexts(dispatcher)),
          calendar = TODAY,
        ),
      writer =
        ScheduleWriter(
          scheduleDao = ScheduleDao(this),
          budgetSync = TestBudgetSync(),
          budgetPrefs = TestBudgetLocalPreferences(),
          uuidGenerator = UuidGenerator { "id-${nextId++}" },
          calendar = TODAY,
          clock = TestClock(NOW),
        ),
      calendar = TODAY,
      viewModelContext = scope.viewModelContext,
    )
  }

  private companion object {
    val TODAY = Calendar { LocalDate(2026, 4, 1) }
    val NOW = Instant.parse("2026-04-01T12:00:00Z")
  }
}
