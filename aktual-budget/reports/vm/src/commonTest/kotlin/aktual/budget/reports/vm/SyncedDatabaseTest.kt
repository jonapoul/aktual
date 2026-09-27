package aktual.budget.reports.vm

import aktual.budget.BudgetSyncController
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.buildDatabase
import aktual.budget.db.dao.SyncDao
import aktual.budget.model.BudgetId
import aktual.budget.model.LocalChange
import aktual.test.inMemoryDriverFactory
import kotlin.time.Clock
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

// Like runDatabaseTest, but changes sent to the sync controller are applied to the database
internal fun runSyncedDatabaseTest(
  action: suspend BudgetDatabase.(TestScope, BudgetSyncController) -> Unit
) = runTest {
  val driver = inMemoryDriverFactory().create(BudgetId("abc-123"))
  driver.use {
    val database = buildDatabase(driver)
    val syncDao = SyncDao(database, driver, Clock.System)
    val controller =
      object : BudgetSyncController {
        override suspend fun syncChanges(changes: List<LocalChange>) {
          syncDao.sendMessages(changes)
        }

        override fun schedule() = Unit
      }
    action(database, this, controller)
  }
}
