package aktual.budget.banksync.vm.link

import aktual.budget.model.AccountSyncSource
import aktual.budget.model.Amount
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface LinkBankAccountState {
  data object Loading : LinkBankAccountState

  /** The account couldn't be loaded, with no [cause] if it doesn't exist. */
  @JvmInline value class Failure(val cause: String?) : LinkBankAccountState

  /** The server has none of the providers that list accounts set up, or there's no server. */
  data object NoProviders : LinkBankAccountState

  /**
   * @property providers The configured providers that list accounts, one of them [selected].
   * @property isLinking The chosen account is being linked.
   */
  @Immutable
  data class Choosing(
    val accountName: String?,
    val providers: ImmutableList<AccountSyncSource>,
    val selected: AccountSyncSource,
    val accounts: ExternalAccounts,
    val isLinking: Boolean = false,
  ) : LinkBankAccountState
}

/** What the selected provider lists. */
@Immutable
sealed interface ExternalAccounts {
  data object Loading : ExternalAccounts

  @JvmInline value class Failure(val cause: String?) : ExternalAccounts

  @JvmInline value class Loaded(val items: ImmutableList<ExternalAccountItem>) : ExternalAccounts
}

/** @property linkedTo The name of the budget account it's already linked to, if any. */
@Immutable
data class ExternalAccountItem(
  val accountId: String,
  val name: String,
  val institution: String?,
  val balance: Amount?,
  val linkedTo: String? = null,
)
