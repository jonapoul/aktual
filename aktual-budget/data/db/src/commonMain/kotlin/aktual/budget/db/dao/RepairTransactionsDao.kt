package aktual.budget.db.dao

import aktual.budget.db.BlankPayees
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.MismatchedCleared
import aktual.budget.db.MismatchedSplits
import aktual.budget.db.withResult
import aktual.budget.model.TransactionId
import app.cash.sqldelight.async.coroutines.awaitAsList
import dev.zacsweers.metro.Inject

@Inject
class RepairTransactionsDao(database: BudgetDatabase) {
  private val queries = database.repairTransactionsQueries

  suspend fun blankPayees(): List<BlankPayees> = queries.withResult { blankPayees().awaitAsList() }

  suspend fun mismatchedCleared(): List<MismatchedCleared> = queries.withResult {
    mismatchedCleared().awaitAsList()
  }

  suspend fun orphanedChildren(): List<TransactionId> = queries.withResult {
    orphanedChildren().awaitAsList()
  }

  suspend fun mismatchedSplits(): List<MismatchedSplits> = queries.withResult {
    mismatchedSplits().awaitAsList()
  }

  suspend fun categorisedTransfers(): List<TransactionId> = queries.withResult {
    categorisedTransfers().awaitAsList()
  }

  suspend fun nonParentErrors(): List<TransactionId> = queries.withResult {
    nonParentErrors().awaitAsList()
  }

  suspend fun categorisedParents(): List<TransactionId> = queries.withResult {
    categorisedParents().awaitAsList()
  }
}
