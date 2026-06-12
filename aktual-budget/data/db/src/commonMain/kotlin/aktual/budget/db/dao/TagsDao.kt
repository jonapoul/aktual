package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.GetTags
import aktual.budget.db.withResult
import app.cash.sqldelight.async.coroutines.awaitAsList
import dev.zacsweers.metro.Inject

@Inject
class TagsDao(database: BudgetDatabase) {
  private val queries = database.tagsQueries

  suspend fun getTags(): List<GetTags> = queries.withResult { getTags().awaitAsList() }
}
