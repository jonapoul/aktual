package aktual.budget.accounts.ui.manage

import aktual.budget.accounts.vm.manage.ManagedAccount
import aktual.budget.accounts.vm.manage.ManagedGroup
import aktual.budget.model.AccountGroupId
import aktual.budget.model.Amount.Companion.Zero
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.toImmutableList

internal enum class Side(val key: String) {
  OnBudget("on"),
  OffBudget("off"),
}

// The groups of each side, in the order shown
@Immutable
internal data class ManageSections(
  val onBudget: List<ManagedGroup>,
  val offBudget: List<ManagedGroup>,
)

internal operator fun ManageSections.get(side: Side): List<ManagedGroup> =
  when (side) {
    OnBudget -> onBudget
    OffBudget -> offBudget
  }

internal fun ManageSections.with(side: Side, groups: List<ManagedGroup>): ManageSections =
  when (side) {
    OnBudget -> copy(onBudget = groups)
    OffBudget -> copy(offBudget = groups)
  }

// One line of a side of the list. An account belongs to the group header above it
@Immutable
internal sealed interface ManageLine {
  val key: String
  val side: Side
  val group: ManagedGroup
}

internal data class GroupLine(override val side: Side, override val group: ManagedGroup) :
  ManageLine {
  override val key = groupKey(side, group.id)
}

internal data class EmptyLine(override val side: Side, override val group: ManagedGroup) :
  ManageLine {
  override val key = groupKey(side, group.id) + "-empty"
}

internal data class AccountLine(
  override val side: Side,
  val account: ManagedAccount,
  override val group: ManagedGroup,
) : ManageLine {
  override val key = "${side.key}-account-${account.id.value}"
}

private fun groupKey(side: Side, id: AccountGroupId?) = "${side.key}-group-${id?.value}"

// Dragging a group collapses every group to its header, so groups only pass other headers
internal fun manageLines(
  side: Side,
  groups: List<ManagedGroup>,
  collapsed: Boolean = false,
): List<ManageLine> = buildList {
  for (group in groups) {
    add(GroupLine(side, group))
    if (collapsed) continue
    if (group.accounts.isEmpty()) add(EmptyLine(side, group))
    group.accounts.forEach { add(AccountLine(side, it, group)) }
  }
}

/**
 * The groups after moving the account with [key] to index [to] of the lines, or null if it can't go
 * there. It joins the group whose header ends up above it, so it can't go above Ungrouped.
 */
internal fun movedAccount(
  side: Side,
  groups: List<ManagedGroup>,
  key: String,
  to: Int,
): List<ManagedGroup>? {
  val lines = manageLines(side, groups)
  val from = lines.indexOfFirst { it.key == key }
  if (lines.getOrNull(from) !is AccountLine || to !in lines.indices || from == to) return null
  val moved = lines.toMutableList().apply { add(to, removeAt(from)) }
  if (moved.first() !is GroupLine) return null
  val result = mutableListOf<Pair<ManagedGroup, MutableList<ManagedAccount>>>()
  for (line in moved) {
    when (line) {
      is GroupLine -> result += line.group to mutableListOf()
      is AccountLine -> result.last().second += line.account
      is EmptyLine -> Unit
    }
  }
  val updated = result.map { (group, accounts) -> group.withAccounts(accounts) }
  return updated.takeIf { it != groups }
}

/**
 * The groups after moving the group with [key] to index [to] of the collapsed lines, or null if it
 * can't go there. Ungrouped stays first.
 */
internal fun movedGroup(
  side: Side,
  groups: List<ManagedGroup>,
  key: String,
  to: Int,
): List<ManagedGroup>? {
  val lines = manageLines(side, groups, collapsed = true)
  val from = lines.indexOfFirst { it.key == key }
  val moving = lines.getOrNull(from) ?: return null
  if (moving.group.id == null || to !in lines.indices || from == to) return null
  val moved = lines.map { it.group }.toMutableList().apply { add(to, removeAt(from)) }
  return moved.takeIf { it.first().id == null }
}

// Sorts the groups to match [order], which holds the same groups
internal fun List<ManagedGroup>.inOrderOf(order: List<ManagedGroup>): List<ManagedGroup> {
  val ids = order.map { it.id }
  return sortedBy { ids.indexOf(it.id) }
}

private fun ManagedGroup.withAccounts(accounts: List<ManagedAccount>) =
  copy(
    accounts = accounts.toImmutableList(),
    total = accounts.fold(Zero) { sum, a -> sum + a.balance },
  )
