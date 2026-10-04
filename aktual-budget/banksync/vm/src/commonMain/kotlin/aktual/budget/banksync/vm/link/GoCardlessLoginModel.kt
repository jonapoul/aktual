package aktual.budget.banksync.vm.link

import aktual.api.client.BankSyncApi
import aktual.api.model.banksync.BankSyncTransactionsResponse.Failure
import aktual.api.model.banksync.BankSyncTransactionsResponse.ProviderError
import aktual.api.model.banksync.ExternalBankAccount
import aktual.api.model.banksync.GoCardlessAccountsResponse
import aktual.api.model.banksync.GoCardlessBanksResponse
import aktual.api.model.banksync.GoCardlessLoginResponse
import aktual.budget.banksync.domain.GoCardlessLoginWaiter
import alakazam.kotlin.requireMessage
import java.util.Locale
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

/**
 * The GoCardless half of linking: picking a bank, then waiting while the user logs in to it in
 * their browser, after which [onAccounts] gets what they shared.
 *
 * See packages/desktop-client/src/components/modals/GoCardlessExternalMsgModal.tsx
 */
internal class GoCardlessLoginModel(
  private val api: BankSyncApi,
  private val waiter: GoCardlessLoginWaiter,
  private val scope: CoroutineScope,
  private val showDemo: Boolean,
  private val openBrowser: suspend (String) -> Unit,
  private val onAccounts: suspend (List<ExternalBankAccount>) -> Unit,
) {
  private val mutableState =
    MutableStateFlow(GoCardlessLogin(COUNTRIES, defaultCountry(), GoCardlessBanks.Loading))
  val state: StateFlow<GoCardlessLogin> = mutableState.asStateFlow()

  private var banksJob: Job? = null
  private var loginJob: Job? = null

  /** Lists the banks in the chosen country, unless they're already listed or loading. */
  fun start() {
    if (banksJob == null || mutableState.value.banks is GoCardlessBanks.Failure) {
      loadBanks(mutableState.value.country)
    }
  }

  fun selectCountry(country: String) {
    if (country == mutableState.value.country) return
    mutableState.update { it.copy(country = country) }
    loadBanks(country)
  }

  /** Starts logging in to [bankId], unless a login's already under way. */
  fun logIn(bankId: String) {
    val banks = mutableState.value.banks as? GoCardlessBanks.Loaded ?: return
    val bank = banks.items.firstOrNull { it.id == bankId } ?: return
    if (loginJob?.isActive == true) return
    loginJob = scope.launch {
      setStatus(GoCardlessLoginStatus.Waiting(bank.name))
      try {
        setStatus(logIn(bank))
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed logging in to ${bank.id} through GoCardless" }
        setStatus(GoCardlessLoginStatus.Failed(e.requireMessage()))
      }
    }
  }

  /** Opens the login page again, in case the browser didn't open the first time. */
  fun reopen() {
    val link = (mutableState.value.status as? GoCardlessLoginStatus.Waiting)?.link ?: return
    scope.launch { openBrowser(link) }
  }

  fun cancel() {
    loginJob?.cancel()
    setStatus(GoCardlessLoginStatus.Idle)
  }

  private suspend fun logIn(bank: GoCardlessBankItem): GoCardlessLoginStatus {
    val login =
      when (val response = api.goCardlessLogin(bank.id)) {
        is GoCardlessLoginResponse.Success -> response
        is GoCardlessLoginResponse.Failed -> return failed(response.error)
      }
    setStatus(GoCardlessLoginStatus.Waiting(bank.name, login.link))
    openBrowser(login.link)
    return when (val response = waiter.await(login.requisitionId)) {
      is GoCardlessAccountsResponse.Success -> {
        onAccounts(response.accounts)
        GoCardlessLoginStatus.Idle
      }
      is GoCardlessAccountsResponse.Failed -> {
        failed(response.error)
      }
      GoCardlessAccountsResponse.Pending -> {
        GoCardlessLoginStatus.Idle
      }
    }
  }

  private fun loadBanks(country: String) {
    banksJob?.cancel()
    mutableState.update { it.copy(banks = GoCardlessBanks.Loading) }
    banksJob = scope.launch {
      val banks =
        try {
          when (val response = api.goCardlessBanks(country, showDemo)) {
            is GoCardlessBanksResponse.Success -> {
              GoCardlessBanks.Loaded(
                response.banks.map { GoCardlessBankItem(it.id, it.name) }.toImmutableList()
              )
            }
            is GoCardlessBanksResponse.Failed -> {
              logcat.w { "Listing GoCardless banks in $country failed: ${response.error}" }
              GoCardlessBanks.Failure(response.error.cause())
            }
          }
        } catch (e: CancellationException) {
          throw e
        } catch (e: Exception) {
          logcat.e(e) { "Failed listing GoCardless banks in $country" }
          GoCardlessBanks.Failure(e.requireMessage())
        }
      mutableState.update { it.copy(banks = banks) }
    }
  }

  private fun setStatus(status: GoCardlessLoginStatus) = mutableState.update {
    it.copy(status = status)
  }

  private fun failed(error: Failure): GoCardlessLoginStatus {
    logcat.w { "GoCardless login failed: $error" }
    val isTimeout = error is ProviderError && error.errorCode == ProviderError.TIMED_OUT
    return GoCardlessLoginStatus.Failed(error.cause().takeUnless { isTimeout }, isTimeout)
  }

  private companion object {
    // packages/desktop-client/src/components/util/countries.ts
    val COUNTRIES: ImmutableList<String> =
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

    // The user's own country if GoCardless supports it, as upstream guesses from the browser
    fun defaultCountry(): String = Locale.getDefault().country.takeIf { it in COUNTRIES } ?: "GB"
  }
}
