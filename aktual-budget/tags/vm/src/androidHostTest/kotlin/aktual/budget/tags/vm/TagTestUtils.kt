package aktual.budget.tags.vm

import aktual.budget.db.BudgetDatabase
import aktual.budget.model.BudgetSyncController
import aktual.budget.model.LocalChange
import aktual.budget.model.TagId

internal class RecordingSyncController : BudgetSyncController {
  val changes = mutableListOf<LocalChange>()

  override suspend fun syncChanges(changes: List<LocalChange>) {
    this.changes += changes
  }

  override fun schedule() = Unit
}

internal suspend fun BudgetDatabase.insertTag(
  id: String,
  tag: String,
  color: String? = null,
  description: String? = null,
) = tagsQueries.insert(id = TagId(id), tag = tag, color = color, description = description)

internal suspend fun BudgetDatabase.tombstoneTag(id: String) =
  tagsQueries.setTombstone(tombstone = true, id = TagId(id))
