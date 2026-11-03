package aktual.budget.schedules.vm.edit

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.ScheduleDao
import aktual.budget.model.Amount
import aktual.budget.model.DbMetadata
import aktual.budget.model.DbMetadataKey.Companion.LastScheduleRun
import aktual.budget.model.RuleId
import aktual.budget.model.ScheduleId
import aktual.test.TestBudgetLocalPreferences
import aktual.test.TestCalendar
import aktual.test.TestSyncController
import aktual.test.insertSchedule
import aktual.test.runDatabaseTest
import alakazam.test.TestClock
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlin.time.Instant
import kotlinx.datetime.LocalDate

class ScheduleWriterTest {
  private val budgetPrefs = TestBudgetLocalPreferences(DbMetadata().set(LastScheduleRun, TODAY))

  @Test
  fun `Creating a schedule that posts lets today's run happen again`() = runDatabaseTest {
    writer().create(FORM)

    assertThat(budgetPrefs[LastScheduleRun]).isNull()
  }

  @Test
  fun `Creating a schedule that doesn't post keeps today's run`() = runDatabaseTest {
    writer().create(FORM.copy(postsTransaction = false))

    assertThat(budgetPrefs[LastScheduleRun]).isEqualTo(TODAY)
  }

  @Test
  fun `Editing a schedule that posts lets today's run happen again`() = runDatabaseTest {
    insertSchedule(id = "a", name = "Rent", payee = "Landlord", account = "Current")

    writer().update(ScheduleId("a"), RuleId("a-rule"), [], [], FORM)

    assertThat(budgetPrefs[LastScheduleRun]).isNull()
  }

  private fun BudgetDatabase.writer() =
    ScheduleWriter(
      scheduleDao = ScheduleDao(this),
      syncController = TestSyncController(),
      budgetPrefs = budgetPrefs,
      uuidGenerator = { "id" },
      calendar = TestCalendar(TODAY),
      clock = TestClock(Instant.parse("2026-04-01T12:00:00Z")),
    )

  private companion object {
    val TODAY = LocalDate(2026, 4, 1)

    val FORM =
      ScheduleForm(
        name = "Rent",
        payee = null,
        account = null,
        amount = ScheduleAmount.Exactly(Amount(-1_000L)),
        date = ScheduleDate.Once(TODAY),
        postsTransaction = true,
      )
  }
}
