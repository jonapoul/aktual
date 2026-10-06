package aktual.budget.banksync.vm.link

import aktual.api.client.BankSyncApi
import aktual.api.client.EnableBankingApi
import aktual.api.model.banksync.BankSyncTransactionsResponse.Failure
import aktual.api.model.banksync.BankSyncTransactionsResponse.ProviderError
import aktual.api.model.banksync.EnableBankingAccountType
import aktual.api.model.banksync.EnableBankingBank
import aktual.api.model.banksync.ExternalBankAccount
import aktual.budget.banksync.domain.GoCardlessLoginWaiter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** A provider that lists accounts only once the user has logged in to their bank. */
internal interface BankLoginProvider {
  val name: String

  /** ISO 3166 codes of the countries whose banks it can list. */
  val countries: ImmutableList<String>

  /** Whether the user picks between logging in as a person or a business. */
  val asksAccountType: Boolean

  suspend fun banks(country: String): LoginResponse<List<LoginBankItem>>

  /** Starts logging in to [bankId], from [banks], as [type] if [asksAccountType]. */
  suspend fun start(bankId: String, type: LoginAccountType?): LoginResponse<LoginLink>

  /** Waits for the user to finish the login [start] gave [token] for, then lists their accounts. */
  suspend fun await(token: String): LoginResponse<List<ExternalBankAccount>>
}

internal sealed interface LoginResponse<out T> {
  data class Success<T>(val value: T) : LoginResponse<T>

  data class Failed(val error: Failure) : LoginResponse<Nothing>
}

/** The user logs in at [link], after which [token] lists the accounts they shared. */
internal data class LoginLink(val link: String, val token: String)

internal class GoCardlessLoginProvider(
  private val api: BankSyncApi,
  private val waiter: GoCardlessLoginWaiter,
  private val showDemo: Boolean,
) : BankLoginProvider {
  override val name = "GoCardless"
  override val countries = COUNTRIES
  override val asksAccountType = false

  override suspend fun banks(country: String) =
    when (val response = api.goCardlessBanks(country, showDemo)) {
      is Success -> {
        LoginResponse.Success(response.banks.map { LoginBankItem(it.id, it.name) })
      }
      is Failed -> {
        LoginResponse.Failed(response.error)
      }
    }

  override suspend fun start(bankId: String, type: LoginAccountType?) =
    when (val response = api.goCardlessLogin(bankId)) {
      is Success -> {
        LoginResponse.Success(LoginLink(response.link, response.requisitionId))
      }
      is Failed -> {
        LoginResponse.Failed(response.error)
      }
    }

  override suspend fun await(token: String) =
    when (val response = waiter.await(token)) {
      is Success -> {
        LoginResponse.Success(response.accounts)
      }
      is Failed -> {
        LoginResponse.Failed(response.error)
      }
      // The waiter polls until it isn't pending, so this is giving up
      Pending -> {
        LoginResponse.Failed(ProviderError(ProviderError.TIMED_OUT, ProviderError.TIMED_OUT))
      }
    }
}

/**
 * Enable Banking names each bank by its name and country, so this keeps the last listed banks to
 * start a login with.
 */
internal class EnableBankingLoginProvider(private val api: EnableBankingApi) : BankLoginProvider {
  override val name = "Enable Banking"
  override val countries = COUNTRIES
  override val asksAccountType = true

  private var banks: Map<String, EnableBankingBank> = emptyMap()

  override suspend fun banks(country: String) =
    when (val response = api.banks(country)) {
      is Success -> {
        banks = response.banks.associateBy(::id)
        LoginResponse.Success(response.banks.map { LoginBankItem(id(it), it.name, it.isBeta) })
      }
      is Failed -> {
        LoginResponse.Failed(response.error)
      }
    }

  override suspend fun start(bankId: String, type: LoginAccountType?): LoginResponse<LoginLink> {
    val bank = banks[bankId] ?: error("No Enable Banking bank $bankId")
    val accountType: EnableBankingAccountType =
      when (type) {
        Business -> Business
        Personal,
        null -> Personal
      }
    return when (val response = api.login(bank, accountType)) {
      is Success -> {
        LoginResponse.Success(LoginLink(response.url, response.state))
      }
      is Failed -> {
        LoginResponse.Failed(response.error)
      }
    }
  }

  override suspend fun await(token: String) =
    when (val response = api.accounts(token)) {
      is Success -> {
        LoginResponse.Success(response.accounts)
      }
      is Failed -> {
        LoginResponse.Failed(response.error)
      }
    }

  private fun id(bank: EnableBankingBank) = "${bank.country}:${bank.name}"
}

// packages/desktop-client/src/components/util/countries.ts, which both providers use
private val COUNTRIES: ImmutableList<String> =
  persistentListOf(
    "AT",
    "BE",
    "BG",
    "HR",
    "CY",
    "CZ",
    "DK",
    "EE",
    "FI",
    "FR",
    "DE",
    "GR",
    "HU",
    "IS",
    "IE",
    "IT",
    "LV",
    "LI",
    "LT",
    "LU",
    "MT",
    "NL",
    "NO",
    "PL",
    "PT",
    "RO",
    "SK",
    "SI",
    "ES",
    "SE",
    "GB",
  )
