package aktual.budget.reports.vm

import aktual.budget.BudgetSyncController
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.buildDatabase
import aktual.budget.db.dao.DatabaseTables.TRANSACTIONS
import aktual.budget.db.dao.SyncDao
import aktual.budget.model.BudgetId
import aktual.budget.model.LocalChange
import aktual.budget.model.localChange
import aktual.test.inMemoryDriverFactory
import kotlin.time.Clock
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

// Like runDatabaseTest, but changes sent to the sync controller are applied to the database
internal fun runSyncedDatabaseTest(
  action: suspend BudgetDatabase.(TestScope, BudgetSyncController) -> Unit,
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

internal suspend fun BudgetSyncController.transaction(
  id: String,
  amount: Long,
  category: String? = null,
  payee: String? = null,
  account: String = "checking",
  date: Long = 20260510,
  isParent: Boolean = false,
  parent: String? = null,
  transfer: String? = null,
  isDeleted: Boolean = false,
) =
  row(
    TRANSACTIONS,
    id,
    "acct" to account,
    "amount" to amount,
    "date" to date,
    "category" to category,
    "description" to payee,
    "isParent" to isParent,
    "isChild" to (parent != null),
    "parent_id" to parent,
    "transferred_id" to transfer,
    "tombstone" to isDeleted,
  )

internal suspend fun BudgetSyncController.row(
  dataset: String,
  id: String,
  vararg values: Pair<String, Any?>,
) =
  syncChanges(
    values.map { (column, value) ->
      when (value) {
        is Long -> localChange(dataset, id, column, value)
        is Boolean -> localChange(dataset, id, column, value)
        else -> localChange(dataset, id, column, value?.toString())
      }
    },
  )
