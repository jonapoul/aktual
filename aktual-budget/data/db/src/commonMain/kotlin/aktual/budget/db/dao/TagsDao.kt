package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.GetTag
import aktual.budget.db.GetTags
import aktual.budget.db.withResult
import aktual.budget.model.TagId
import alakazam.kotlin.CoroutineContexts
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Inject
class TagsDao(database: BudgetDatabase, private val contexts: CoroutineContexts) {
  private val queries = database.tagsQueries

  suspend fun getTags(): List<GetTags> = queries.withResult { getTags().awaitAsList() }

  fun observeTags(): Flow<List<GetTags>> =
    queries.getTags().asFlow().mapToList(contexts.default).distinctUntilChanged()

  suspend fun getTag(id: TagId): GetTag? = queries.withResult { getTag(id).awaitAsOneOrNull() }

  suspend fun getTagIdByName(name: String): TagId? = queries.withResult {
    getTagIdByName(name).awaitAsOneOrNull()
  }

  suspend fun insert(id: TagId, tag: String, color: String?, description: String?): Long =
    queries.withResult {
      insert(id = id, tag = tag, color = color, description = description)
    }
}
