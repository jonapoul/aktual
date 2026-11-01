package aktual.budget.navrail.vm

import aktual.budget.db.GetAllWithBalances
import aktual.budget.model.AccountGroup
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSyncState
import aktual.budget.model.Amount
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

  operator fun get(group: AccountGroup): DrawerAccountSection =
    when (group) {
      OnBudget -> onBudget
      OffBudget -> offBudget
      Closed -> closed
    }
}

@Immutable
data class DrawerAccountSection(
  val accounts: ImmutableList<DrawerAccount> = persistentListOf(),
  val total: Amount = Zero,
)

@Immutable
data class DrawerAccount(
  val id: AccountId,
  val name: String,
  val balance: Amount,
  val syncState: AccountSyncState = NotLinked,
  val newTransactions: Int = 0,
)

internal fun List<GetAllWithBalances>.toDrawerAccounts(
  newTransactions: Map<AccountId, Int> = emptyMap(),
): DrawerAccounts {
  val (closed, open) = partition { it.closed == true }
  val (offBudget, onBudget) = open.partition { it.offbudget == true }
  return DrawerAccounts(
    onBudget = onBudget.toSection(newTransactions),
    offBudget = offBudget.toSection(newTransactions),
    closed = closed.toSection(newTransactions),
  )
}

private fun List<GetAllWithBalances>.toSection(
  newTransactions: Map<AccountId, Int>,
): DrawerAccountSection {
  val accounts = map { row ->
    DrawerAccount(
      id = row.id,
      name = row.name.orEmpty(),
      balance = Amount(row.balance),
      syncState =
        AccountSyncState(
          row.account_sync_source,
          row.account_id,
          row.bank_sync_status,
          row.last_sync,
        ),
      newTransactions = newTransactions[row.id] ?: 0,
    )
  }
  return DrawerAccountSection(
    accounts = accounts.toImmutableList(),
    total = accounts.fold(Zero) { sum, account -> sum + account.balance },
  )
}
