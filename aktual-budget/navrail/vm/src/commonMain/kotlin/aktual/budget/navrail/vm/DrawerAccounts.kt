package aktual.budget.navrail.vm

import aktual.budget.db.GetAllWithBalances
import aktual.budget.model.AccountGroup
import aktual.budget.model.AccountId
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

@Immutable data class DrawerAccount(val id: AccountId, val name: String, val balance: Amount)

internal fun List<GetAllWithBalances>.toDrawerAccounts(): DrawerAccounts {
  val (closed, open) = partition { it.closed == true }
  val (offBudget, onBudget) = open.partition { it.offbudget == true }
  return DrawerAccounts(
    onBudget = onBudget.toSection(),
    offBudget = offBudget.toSection(),
    closed = closed.toSection(),
  )
}

private fun List<GetAllWithBalances>.toSection(): DrawerAccountSection {
  val accounts = map { DrawerAccount(it.id, it.name.orEmpty(), Amount(it.balance)) }
  return DrawerAccountSection(
    accounts = accounts.toImmutableList(),
    total = accounts.fold(Amount.Zero) { sum, account -> sum + account.balance },
  )
}
