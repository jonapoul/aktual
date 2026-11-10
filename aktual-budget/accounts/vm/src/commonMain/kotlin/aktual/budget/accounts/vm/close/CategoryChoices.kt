package aktual.budget.accounts.vm.close

import aktual.budget.db.GetAllGrouped
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

// Leaves out hidden categories and groups, and groups left with none
internal fun List<GetAllGrouped>.toCategoryGroups(): ImmutableList<CategoryChoiceGroup> =
  filter(::isVisible)
    .groupBy(GetAllGrouped::groupId)
    .values
    .mapNotNull(::toCategoryGroup)
    .toImmutableList()

private fun isVisible(grouped: GetAllGrouped): Boolean =
  !grouped.hidden && grouped.groupHidden != true

private fun toCategoryGroup(rows: List<GetAllGrouped>): CategoryChoiceGroup? {
  val categories =
    rows.mapNotNull { row -> row.name?.let { CategoryChoice(row.id, it) } }.toImmutableList()
  val name = rows.first().groupName.orEmpty()
  return categories.takeIf { it.isNotEmpty() }?.let { CategoryChoiceGroup(name, it) }
}
