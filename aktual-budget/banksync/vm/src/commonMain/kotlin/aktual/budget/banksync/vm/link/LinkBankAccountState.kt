package aktual.budget.banksync.vm.link

import aktual.budget.model.AccountSyncSource
import aktual.budget.model.Amount
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface LinkBankAccountState {
  data object Loading : LinkBankAccountState

  @JvmInline value class Failure(val cause: String?) : LinkBankAccountState

  data object NoProviders : LinkBankAccountState

  @Immutable
  data class Choosing(
    val accountName: String?,
    val providers: ImmutableList<AccountSyncSource>,
    val selected: AccountSyncSource,
    val accounts: ExternalAccounts,
    val isLinking: Boolean = false,
  ) : LinkBankAccountState
}

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
  val status: GoCardlessLoginStatus = Idle,
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

@Immutable
data class ExternalAccountItem(
  val accountId: String,
  val name: String,
  val institution: String?,
  val balance: Amount?,
  val linkedTo: String? = null,
)
