package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.withResult
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import dev.zacsweers.metro.Inject

@Inject
class NotesDao(database: BudgetDatabase) {
  private val queries = database.notesQueries

  suspend fun getNote(id: String): String? = queries.withResult {
    getNote(id).awaitAsOneOrNull()?.note
  }
}
