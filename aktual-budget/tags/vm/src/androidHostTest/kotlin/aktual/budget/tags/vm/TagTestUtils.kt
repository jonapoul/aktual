package aktual.budget.tags.vm

import aktual.budget.db.BudgetDatabase
import aktual.budget.model.TagId

internal suspend fun BudgetDatabase.insertTag(
  id: String,
  tag: String,
  color: String? = null,
  description: String? = null,
) = tagsQueries.insert(id = TagId(id), tag = tag, color = color, description = description)

internal suspend fun BudgetDatabase.tombstoneTag(id: String) =
  tagsQueries.setTombstone(tombstone = true, id = TagId(id))
