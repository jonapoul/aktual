package aktual.budget.banksync.ui

import aktual.budget.banksync.vm.BankSyncAccount
import aktual.budget.banksync.vm.BankSyncAccountStatus
import aktual.budget.banksync.vm.BankSyncProvider
import aktual.budget.banksync.vm.LastBankSync
import aktual.budget.banksync.vm.Success
import aktual.budget.model.AccountId
import kotlinx.collections.immutable.persistentListOf

internal object BankSyncPreview {
  val checking =
    BankSyncAccount(
      id = AccountId("checking"),
      name = "Checking",
      bankName = "Bankity Bank",
      lastSync = LastBankSync.MinutesAgo(minutes = 5),
      status = BankSyncAccountStatus.Ok,
    )

  val savings =
    BankSyncAccount(
      id = AccountId("savings"),
      name = "Savings",
      bankName = "Bankity Bank",
      lastSync = LastBankSync.DaysAgo(days = 3),
      status = BankSyncAccountStatus.ReauthRequired,
    )

  val creditCard =
    BankSyncAccount(
      id = AccountId("credit"),
      name = "Credit card with a very long name that doesn't fit",
      bankName = null,
      lastSync = Never,
      status = null,
    )

  val cash =
    BankSyncAccount(
      id = AccountId("cash"),
      name = "Cash",
      bankName = null,
      lastSync = Never,
      status = null,
    )

  val success =
    Success(
      providers =
        persistentListOf(
          BankSyncProvider(
            source = GoCardless,
            status = Configured,
            accounts = persistentListOf(checking, savings),
          ),
          BankSyncProvider(
            source = SimpleFin,
            status = NotConfigured,
            accounts =
              persistentListOf(
                creditCard,
                checking.copy(
                  id = AccountId("other"),
                  name = "Joint",
                  lastSync = LastBankSync.HoursAgo(hours = 1),
                  status = RateLimited,
                ),
              ),
          ),
        ),
      unlinked = persistentListOf(cash),
    )

  val unlinkedOnly = Success(providers = persistentListOf(), unlinked = persistentListOf(cash))
}
