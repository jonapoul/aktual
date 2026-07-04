package aktual.budget.tags.vm.list

import aktual.budget.db.GetTag
import aktual.budget.db.GetTags
import aktual.budget.model.TagId
import aktual.budget.tags.vm.toColorOrNull

internal fun GetTags.toTagItem(numTransactions: Int = 0): TagItem? =
  toTagItem(id, tag, color, description, hidden, numTransactions)

internal fun GetTag.toTagItem(numTransactions: Int = 0): TagItem? =
  toTagItem(id, tag, color, description, hidden, numTransactions)

private fun toTagItem(
  id: TagId,
  tag: String?,
  color: String?,
  description: String?,
  hidden: Boolean?,
  numTransactions: Int,
): TagItem? {
  val name = tag ?: return null
  return TagItem(
    id = id,
    tag = name,
    color = color?.toColorOrNull(),
    description = description.orEmpty(),
    hidden = hidden == true,
    numTransactions = numTransactions,
  )
}
