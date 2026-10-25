package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.withResult
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.model.TransactionId
import app.cash.sqldelight.async.coroutines.awaitAsList
import dev.zacsweers.metro.Inject
import kotlinx.datetime.LocalDate

data class LearningChange(val id: TransactionId, val payee: PayeeId?, val date: LocalDate)

data class LearningTransaction(
  val id: TransactionId,
  val payee: PayeeId,
  val category: CategoryId?,
)

@Inject
class CategoryLearningDao(database: BudgetDatabase) {
  private val queries = database.categoryLearningQueries

  suspend fun changed(ids: Collection<TransactionId>): List<LearningChange> = queries.withResult {
    learningChanged(ids) { id, payee, date -> LearningChange(id, payee, date) }.awaitAsList()
  }

  // Newest first
  suspend fun history(
    payees: Collection<PayeeId>,
    from: LocalDate,
    to: LocalDate,
  ): List<LearningTransaction> = queries.withResult {
    learningHistory(from, to, payees) { id, payee, category ->
        LearningTransaction(id, requireNotNull(payee), category)
      }
      .awaitAsList()
  }
}
