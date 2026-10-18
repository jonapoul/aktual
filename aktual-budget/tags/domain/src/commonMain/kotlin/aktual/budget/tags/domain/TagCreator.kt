package aktual.budget.tags.domain

import aktual.budget.BudgetSyncController
import aktual.budget.db.dao.DatabaseTables.TAGS
import aktual.budget.db.dao.TagsDao
import aktual.budget.model.TagId
import aktual.budget.model.localChange
import aktual.budget.model.untombstone
import aktual.core.UuidGenerator
import dev.zacsweers.metro.Inject

// Creates a tag with just a name, as upstream's tags-create does from a transaction's notes. A tag
// that already has the name is kept as it is, and a deleted one is brought back under its old id
@Inject
class TagCreator(
  private val tagsDao: TagsDao,
  private val uuidGenerator: UuidGenerator,
  private val syncController: BudgetSyncController,
) {
  suspend fun create(tag: String): TagId {
    val owner = tagsDao.getTagIdByName(tag)
    if (owner != null && tagsDao.getTag(owner) != null) return owner

    val id = owner ?: uuidGenerator(::TagId)
    val none: String? = null
    tagsDao.insert(id = id, tag = tag, color = none, description = none)
    val row = id.toString()
    syncController.syncChanges(
      localChange(TAGS, row, "id", row),
      localChange(TAGS, row, "tag", tag),
      localChange(TAGS, row, "color", none),
      localChange(TAGS, row, "description", none),
      untombstone(TAGS, row),
    )
    return id
  }
}
