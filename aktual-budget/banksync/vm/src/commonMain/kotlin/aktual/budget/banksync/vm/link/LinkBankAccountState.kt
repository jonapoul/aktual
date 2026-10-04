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

  /** The server has no providers set up for linking, or there's no server. */
  data object NoProviders : LinkBankAccountState

  /**
   * @property providers The configured providers that can link accounts, one of them [selected].
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

  /** GoCardless lists accounts only once the user has logged in to their bank. */
  @JvmInline value class NeedsLogin(val login: GoCardlessLogin) : ExternalAccounts
}

/**
 * Picking a bank to log in to through GoCardless.
 *
 * @property countries ISO 3166 codes of the countries GoCardless supports, one of them [country].
 * @property banks The banks in [country].
 */
@Immutable
data class GoCardlessLogin(
  val countries: ImmutableList<String>,
  val country: String,
  val banks: GoCardlessBanks,
  val status: GoCardlessLoginStatus = GoCardlessLoginStatus.Idle,
)

@Immutable
sealed interface GoCardlessBanks {
  data object Loading : GoCardlessBanks

  @JvmInline value class Failure(val cause: String?) : GoCardlessBanks

  @JvmInline value class Loaded(val items: ImmutableList<GoCardlessBankItem>) : GoCardlessBanks
}

@Immutable data class GoCardlessBankItem(val id: String, val name: String)

@Immutable
sealed interface GoCardlessLoginStatus {
  data object Idle : GoCardlessLoginStatus

  /**
   * Waiting for the user to log in to [bank] in their browser, at [link] once GoCardless has given
   * one.
   */
  data class Waiting(val bank: String, val link: String? = null) : GoCardlessLoginStatus

  /** The login failed, or the user took too long if [isTimeout]. */
  data class Failed(val cause: String?, val isTimeout: Boolean = false) : GoCardlessLoginStatus
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
