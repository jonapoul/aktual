package aktual.budget.accounts.ui.manage

import aktual.budget.accounts.vm.manage.AccountGroupItem
import aktual.budget.accounts.vm.manage.AccountSection
import aktual.budget.accounts.vm.manage.ManagedAccount
import aktual.budget.accounts.vm.manage.ManagedGroup
import aktual.budget.accounts.vm.manage.Success
import aktual.budget.model.AccountGroupId
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import kotlinx.collections.immutable.persistentListOf

internal object ManageAccountsPreview {
  val everyday = AccountGroupItem(AccountGroupId("everyday"), "Everyday")
  val savings = AccountGroupItem(AccountGroupId("savings"), "Savings")
  private val investments = AccountGroupItem(AccountGroupId("investments"), "Investments")

  private val current =
    account("current", "Current account", 1234.56, 412, everyday, hasNotes = true)
  private val credit = account("credit", "Credit card", -245.10, 88, everyday)
  private val rainyDay = account("rainy", "Rainy day", 5000.0, 12, savings)
  private val wallet = account("wallet", "Wallet", 20.0, 1, null)
  private val pension = account("pension", "Pension", 42000.0, 30, null, offBudget = true)

  val success =
    Success(
      onBudget =
        AccountSection(
          total = Amount(6009.46),
          groups =
            persistentListOf(
              ManagedGroup(null, "", Amount(20.0), persistentListOf(wallet)),
              ManagedGroup(
                everyday.id,
                everyday.name,
                Amount(989.46),
                persistentListOf(current, credit),
              ),
              ManagedGroup(savings.id, savings.name, Amount(5000.0), persistentListOf(rainyDay)),
            ),
        ),
      offBudget =
        AccountSection(
          total = Amount(42000.0),
          groups =
            persistentListOf(
              ManagedGroup(null, "", Amount(42000.0), persistentListOf(pension)),
              ManagedGroup(investments.id, investments.name, Amount(0.0), persistentListOf()),
            ),
        ),
      groups = persistentListOf(everyday, savings, investments),
      closed = persistentListOf(account("old", "Old savings", 0.0, 54, null)),
      closedExpanded = true,
    )

  private fun account(
    id: String,
    name: String,
    balance: Double,
    transactions: Long,
    group: AccountGroupItem?,
    offBudget: Boolean = false,
    hasNotes: Boolean = false,
  ) =
    ManagedAccount(
      id = AccountId(id),
      name = name,
      balance = Amount(balance),
      transactionCount = transactions,
      hasNotes = hasNotes,
      offBudget = offBudget,
      group = group?.id,
    )
}
