package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.GetTag
import aktual.budget.db.GetTags
import aktual.budget.db.withResult
import aktual.budget.model.TagId
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import dev.zacsweers.metro.Inject

@Inject
class TagsDao(database: BudgetDatabase) {
  private val queries = database.tagsQueries

  suspend fun getTags(): List<GetTags> = queries.withResult { getTags().awaitAsList() }

  suspend fun getTag(id: TagId): GetTag? = queries.withResult { getTag(id).awaitAsOneOrNull() }

  suspend fun insert(id: TagId, tag: String, color: String?, description: String?): Long =
    queries.withResult {
      insert(id = id, tag = tag, color = color, description = description)
    }
}
