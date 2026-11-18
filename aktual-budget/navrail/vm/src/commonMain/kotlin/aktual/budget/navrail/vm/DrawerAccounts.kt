package aktual.budget.navrail.vm

import aktual.budget.db.GetAllWithBalances
import aktual.budget.db.accountGroups.GetAllActive
import aktual.budget.model.AccountGroup
import aktual.budget.model.AccountGroupId
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSyncState
import aktual.budget.model.Amount
import aktual.budget.model.sumOf
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Immutable
data class DrawerAccounts(
  val onBudget: DrawerAccountSection = DrawerAccountSection(),
  val offBudget: DrawerAccountSection = DrawerAccountSection(),
  val closed: DrawerAccountSection = DrawerAccountSection(),
) {
  // Closed accounts don't count, as upstream
  val total: Amount
    get() = onBudget.total + offBudget.total

  // Upstream only leaves room for sync dots when some open account is linked
  val hasLinkedAccounts: Boolean
    get() =
      sequenceOf(onBudget, offBudget).flatMap { it.accounts }.any { it.syncState != NotLinked }

  operator fun get(group: AccountGroup): DrawerAccountSection =
    when (group) {
      OnBudget -> onBudget
      OffBudget -> offBudget
      Closed -> closed
    }
}

/**
 * Accounts not in a group come first, then each group holding any. Totals and counts are for the
 * whole section, so they stay the same while a search hides some accounts.
 */
@Immutable
data class DrawerAccountSection(
  val buckets: ImmutableList<DrawerAccountBucket> = persistentListOf(),
  val total: Amount = buckets.sumOf { it.total },
  val accountCount: Int = buckets.map { it.accounts.size }.sum(),
  val failedCount: Int = buckets.map { it.failedCount }.sum(),
) {
  val accounts: List<DrawerAccount>
    get() = buckets.flatMap { it.accounts }
}

@Immutable
data class DrawerAccountBucket(
  val group: DrawerAccountGroup?,
  val accounts: ImmutableList<DrawerAccount>,
  val total: Amount = accounts.sumOf { it.balance },
  val failedCount: Int = accounts.count { it.syncState is AccountSyncState.Failed },
)

@Immutable data class DrawerAccountGroup(val id: AccountGroupId, val name: String)

@Immutable
data class DrawerAccount(
  val id: AccountId,
  val name: String,
  val balance: Amount,
  val syncState: AccountSyncState = NotLinked,
  val newTransactions: Int = 0,
)

// packages/desktop-client/src/components/sidebar/redesign/useSidebarAccountTree.ts
internal fun List<GetAllWithBalances>.toDrawerAccounts(
  groups: List<GetAllActive> = emptyList(),
  newTransactions: Map<AccountId, Int> = emptyMap(),
): DrawerAccounts {
  val (closed, open) = partition { it.closed == true }
  val (offBudget, onBudget) = open.partition { it.offbudget == true }
  val drawerGroups = groups.map { DrawerAccountGroup(it.id, it.name.orEmpty()) }
  return DrawerAccounts(
    onBudget = onBudget.toSection(drawerGroups, newTransactions),
    offBudget = offBudget.toSection(drawerGroups, newTransactions),
    // Closed accounts aren't grouped
    closed = closed.toSection(emptyList(), newTransactions),
  )
}

private fun List<GetAllWithBalances>.toSection(
  groups: List<DrawerAccountGroup>,
  newTransactions: Map<AccountId, Int>,
): DrawerAccountSection {
  val groupIds = groups.mapTo(mutableSetOf()) { it.id }
  val byGroup =
    groupBy { row -> row.account_group_id?.takeIf { it in groupIds } }
      .mapValues { (_, rows) -> rows.map { it.toDrawerAccount(newTransactions) } }

  val buckets = buildList {
    byGroup[null]?.let { add(DrawerAccountBucket(group = null, it.toImmutableList())) }
    groups.forEach { group ->
      byGroup[group.id]?.let { add(DrawerAccountBucket(group, it.toImmutableList())) }
    }
  }
  return DrawerAccountSection(buckets.toImmutableList())
}

private fun GetAllWithBalances.toDrawerAccount(newTransactions: Map<AccountId, Int>) =
  DrawerAccount(
    id = id,
    name = name.orEmpty(),
    balance = Amount(balance),
    syncState = AccountSyncState(account_sync_source, account_id, bank_sync_status, last_sync),
    newTransactions = newTransactions[id] ?: 0,
  )

/**
 * Keeps the accounts whose name, or group name, contains [query]. As upstream's
 * filterSidebarTree, groups left empty are dropped.
 */
internal fun DrawerAccounts.filter(query: String): DrawerAccounts {
  val trimmed = query.trim()
  if (trimmed.isEmpty()) return this
  return DrawerAccounts(
    onBudget = onBudget.filter(trimmed),
    offBudget = offBudget.filter(trimmed),
    closed = closed.filter(trimmed),
  )
}

private fun DrawerAccountSection.filter(query: String): DrawerAccountSection =
  copy(
    buckets =
      buckets
        .mapNotNull { bucket ->
          val groupName = bucket.group?.name.orEmpty()
          val accounts =
            bucket.accounts.filter { "$groupName ${it.name}".contains(query, ignoreCase = true) }
          if (accounts.isEmpty()) null else bucket.copy(accounts = accounts.toImmutableList())
        }
        .toImmutableList()
  )
