package aktual.budget.schedules.domain

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.ScheduleDao
import aktual.budget.db.withoutResult
import aktual.budget.model.ScheduleId
import aktual.budget.model.ScheduleNextDateId
import aktual.budget.model.SyncedPrefKey.Global.UpcomingScheduledTransactionLength
import aktual.budget.model.UpcomingLength
import aktual.core.Calendar
import aktual.test.assertThatNextEmission
import aktual.test.insertSchedule
import aktual.test.runDatabaseTest
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.test
import assertk.Assert
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.extracting
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlin.time.Instant
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.datetime.LocalDate

class SchedulesLoaderTest {
  @Test
  fun `Upcoming window defaults to seven days`() = runDatabaseTest { scope ->
    insert(id = "a", name = "Soon", nextDate = LocalDate(2026, 4, 8))
    insert(id = "b", name = "Later", nextDate = LocalDate(2026, 4, 9))

    assertThat(loader(scope).load())
      .namesAndStatuses()
      .containsExactly("Soon" to ScheduleStatus.Upcoming, "Later" to ScheduleStatus.Scheduled)
  }

  @Test
  fun `Global upcoming length is respected`() = runDatabaseTest { scope ->
    insert(id = "a", name = "Soon", nextDate = LocalDate(2026, 4, 8))
    insert(id = "b", name = "Later", nextDate = LocalDate(2026, 4, 15))
    insert(id = "c", name = "Far", nextDate = LocalDate(2026, 4, 16))
    preferences(scope)[UpcomingScheduledTransactionLength] = UpcomingLength.Weeks(2).encode()

    assertThat(loader(scope).load())
      .namesAndStatuses()
      .containsExactly(
        "Soon" to ScheduleStatus.Upcoming,
        "Later" to ScheduleStatus.Upcoming,
        "Far" to ScheduleStatus.Scheduled,
      )
    assertThat(loader(scope).load(ScheduleId("b"))?.status).isEqualTo(Upcoming)
  }

  @Test
  fun `Custom upcoming length overrides the global one`() = runDatabaseTest { scope ->
    insert(id = "a", name = "Short", nextDate = LocalDate(2026, 4, 8))
    insert(id = "b", name = "Long", nextDate = LocalDate(2026, 4, 25))
    insert(id = "c", name = "Global", nextDate = LocalDate(2026, 4, 26))
    setCustomUpcomingLength("a", UpcomingLength.Days(3))
    setCustomUpcomingLength("b", OneMonth)
    preferences(scope)[UpcomingScheduledTransactionLength] = UpcomingLength.Weeks(2).encode()

    assertThat(loader(scope).load())
      .namesAndStatuses()
      .containsExactly(
        "Short" to ScheduleStatus.Scheduled,
        "Long" to ScheduleStatus.Upcoming,
        "Global" to ScheduleStatus.Scheduled,
      )
  }

  @Test
  fun `Invalid global upcoming length falls back to the default`() = runDatabaseTest { scope ->
    insert(id = "a", name = "Soon", nextDate = LocalDate(2026, 4, 8))
    preferences(scope)[UpcomingScheduledTransactionLength] = "nonsense"

    assertThat(loader(scope).load())
      .namesAndStatuses()
      .containsExactly("Soon" to ScheduleStatus.Upcoming)
  }

  @Test
  fun `Upcoming includes missed and due schedules within the window`() = runDatabaseTest { scope ->
    insert(id = "a", name = "Upcoming", nextDate = LocalDate(2026, 4, 5))
    insert(id = "b", name = "Due", nextDate = TODAY)
    insert(id = "c", name = "Missed", nextDate = LocalDate(2026, 3, 20))
    insert(id = "d", name = "Scheduled", nextDate = LocalDate(2026, 5, 1))
    insert(id = "e", name = "Completed", nextDate = LocalDate(2026, 4, 2), completed = true)
    insert(id = "f", name = "Outside", nextDate = LocalDate(2026, 4, 20))
    setCustomUpcomingLength("f", OneMonth)

    val upcoming = loader(scope).load().upcoming(TODAY, DefaultUpcomingLength)

    assertThat(upcoming)
      .namesAndStatuses()
      .containsExactly(
        "Missed" to ScheduleStatus.Missed,
        "Due" to ScheduleStatus.Due,
        "Upcoming" to ScheduleStatus.Upcoming,
      )
  }

  @Test
  fun `Observing emits again when the next date or global length changes`() =
    runDatabaseTest { scope ->
      insert(id = "a", name = "Rent", nextDate = LocalDate(2026, 4, 15))

      loader(scope).observe().test {
        assertThatNextEmission()
          .namesAndStatuses()
          .containsExactly("Rent" to ScheduleStatus.Scheduled)

        preferences(scope)[UpcomingScheduledTransactionLength] = UpcomingLength.Weeks(2).encode()
        assertThatNextEmission()
          .namesAndStatuses()
          .containsExactly("Rent" to ScheduleStatus.Upcoming)

        schedulesNextDateQueries.withoutResult {
          updateLocalDates(TODAY, Instant.fromEpochMilliseconds(0), ScheduleNextDateId("a-next"))
        }
        assertThatNextEmission().namesAndStatuses().containsExactly("Rent" to ScheduleStatus.Due)

        cancelAndIgnoreRemainingEvents()
      }
    }

  private fun Assert<List<Schedule>>.namesAndStatuses() =
    extracting(Schedule::name, Schedule::status)

  private suspend fun BudgetDatabase.insert(
    id: String,
    name: String,
    nextDate: LocalDate,
    completed: Boolean = false,
  ) =
    insertSchedule(
      id = id,
      name = name,
      payee = "Payee",
      account = "Account",
      nextDate = nextDate,
      completed = completed,
    )

  private suspend fun BudgetDatabase.setCustomUpcomingLength(id: String, length: UpcomingLength) =
    schedulesQueries.withoutResult {
      setCustomUpcomingLength(length, ScheduleId(id))
    }

  private fun BudgetDatabase.preferences(scope: TestScope) =
    PreferencesDao(this, TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler)))

  private fun BudgetDatabase.loader(scope: TestScope) =
    SchedulesLoader(
      scheduleDao = ScheduleDao(this),
      accountDao = AccountDao(this),
      payeeDao = PayeeDao(this),
      preferencesDao = preferences(scope),
      calendar = Calendar { TODAY },
    )

  private companion object {
    val TODAY = LocalDate(2026, 4, 1)
  }
}
