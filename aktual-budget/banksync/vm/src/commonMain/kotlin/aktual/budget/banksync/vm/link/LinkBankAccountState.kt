package aktual.budget.banksync.vm.link

import aktual.budget.model.AccountSyncSource
import aktual.budget.model.Amount
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface LinkBankAccountState {
  data object Loading : LinkBankAccountState

  @JvmInline value class Failure(val cause: String?) : LinkBankAccountState

  @JvmInline value class NoProviders(val hasServer: Boolean) : LinkBankAccountState

  @Immutable
  data class Choosing(
    val target: LinkTarget,
    val providers: ImmutableList<AccountSyncSource>,
    val selected: AccountSyncSource,
    val accounts: ExternalAccounts,
    val isLinking: Boolean = false,
  ) : LinkBankAccountState
}

@Immutable
sealed interface LinkTarget {
  @JvmInline value class Existing(val name: String?) : LinkTarget

  @JvmInline value class New(val offBudget: Boolean = false) : LinkTarget
}

@Immutable
sealed interface ExternalAccounts {
  data object Loading : ExternalAccounts

  @JvmInline value class Failure(val cause: String?) : ExternalAccounts

  @JvmInline value class Loaded(val items: ImmutableList<ExternalAccountItem>) : ExternalAccounts

  @JvmInline value class NeedsLogin(val login: BankLogin) : ExternalAccounts
}

@Immutable
data class BankLogin(
  val countries: ImmutableList<String>,
  val country: String,
  val banks: LoginBanks,
  val status: BankLoginStatus = Idle,
  val accountType: LoginAccountType? = null,
)

@Immutable
sealed interface LoginBanks {
  data object Loading : LoginBanks

  @JvmInline value class Failure(val cause: String?) : LoginBanks

  @JvmInline value class Loaded(val items: ImmutableList<LoginBankItem>) : LoginBanks
}

@Immutable data class LoginBankItem(val id: String, val name: String, val isBeta: Boolean = false)

enum class LoginAccountType {
  Personal,
  Business,
}

@Immutable
sealed interface BankLoginStatus {
  data object Idle : BankLoginStatus

  data class Waiting(val bank: String, val link: String? = null) : BankLoginStatus

  data class Failed(val cause: String?, val isTimeout: Boolean = false) : BankLoginStatus
}

@Immutable
data class ExternalAccountItem(
  val accountId: String,
  val name: String,
  val institution: String?,
  val balance: Amount?,
  val linkedTo: String? = null,
)
