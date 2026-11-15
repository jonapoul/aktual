package aktual.budget.tags.domain

import aktual.budget.db.dao.DatabaseTables.TAGS
import aktual.budget.db.dao.TagsDao
import aktual.budget.model.TagId
import aktual.budget.model.localChange
import aktual.budget.model.untombstone
import aktual.budget.utils.BudgetSync
import aktual.core.utils.UuidGenerator
import aktual.di.BudgetScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(BudgetScope::class)
class TagCreatorImpl(
  private val tagsDao: TagsDao,
  private val uuidGenerator: UuidGenerator,
  private val budgetSync: BudgetSync,
) : TagCreator {
  override suspend fun create(tag: String): TagId {
    val owner = tagsDao.getTagIdByName(tag)
    if (owner != null && tagsDao.getTag(owner) != null) return owner

    val id = owner ?: uuidGenerator(::TagId)
    val none: String? = null
    tagsDao.insert(id = id, tag = tag, color = none, description = none)
    val row = id.toString()
    budgetSync(
      localChange(TAGS, row, "id", row),
      localChange(TAGS, row, "tag", tag),
      localChange(TAGS, row, "color", none),
      localChange(TAGS, row, "description", none),
      untombstone(TAGS, row),
    )
    return id
  }
}
