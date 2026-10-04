package aktual.budget.banksync.vm.link

import aktual.api.model.banksync.BankSyncTransactionsResponse.Failure
import aktual.api.model.banksync.BankSyncTransactionsResponse.ProviderError
import aktual.api.model.banksync.ExternalBankAccount
import alakazam.kotlin.requireMessage
import java.util.Locale
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
 * Linking through a provider that lists accounts only after the user logs in to their bank: picking
 * the bank, then waiting while the user logs in to it in their browser, after which [onAccounts]
 * gets what they shared.
 *
 * See packages/desktop-client/src/components/modals/GoCardlessExternalMsgModal.tsx and
 * EnableBankingInitialiseModal.tsx
 */
internal class BankLoginModel(
  private val provider: BankLoginProvider,
  private val scope: CoroutineScope,
  private val openBrowser: suspend (String) -> Unit,
  private val onAccounts: suspend (List<ExternalBankAccount>) -> Unit,
) {
  private val mutableState =
    MutableStateFlow(
      BankLogin(
        countries = provider.countries,
        country = defaultCountry(provider.countries),
        banks = Loading,
        accountType = LoginAccountType.Personal.takeIf { provider.asksAccountType },
      )
    )
  val state: StateFlow<BankLogin> = mutableState.asStateFlow()

  private var banksJob: Job? = null
  private var loginJob: Job? = null

  /** Lists the banks in the chosen country, unless they're already listed or loading. */
  fun start() {
    if (banksJob == null || mutableState.value.banks is LoginBanks.Failure) {
      loadBanks(mutableState.value.country)
    }
  }

  fun selectCountry(country: String) {
    if (country == mutableState.value.country) return
    mutableState.update { it.copy(country = country) }
    loadBanks(country)
  }

  fun selectAccountType(type: LoginAccountType) {
    if (!provider.asksAccountType) return
    mutableState.update { it.copy(accountType = type) }
  }

  /** Starts logging in to [bankId], unless a login's already under way. */
  fun logIn(bankId: String) {
    val banks = mutableState.value.banks as? LoginBanks.Loaded ?: return
    val bank = banks.items.firstOrNull { it.id == bankId } ?: return
    if (loginJob?.isActive == true) return
    loginJob = scope.launch {
      setStatus(BankLoginStatus.Waiting(bank.name))
      try {
        setStatus(logIn(bank))
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed logging in to ${bank.id} through ${provider.name}" }
        setStatus(BankLoginStatus.Failed(e.requireMessage()))
      }
    }
  }

  /** Opens the login page again, in case the browser didn't open the first time. */
  fun reopen() {
    val link = (mutableState.value.status as? BankLoginStatus.Waiting)?.link ?: return
    scope.launch { openBrowser(link) }
  }

  fun cancel() {
    loginJob?.cancel()
    setStatus(Idle)
  }

  private suspend fun logIn(bank: LoginBankItem): BankLoginStatus {
    val login =
      when (val response = provider.start(bank.id, mutableState.value.accountType)) {
        is Success -> response.value
        is Failed -> return failed(response.error)
      }
    setStatus(BankLoginStatus.Waiting(bank.name, login.link))
    openBrowser(login.link)
    return when (val response = provider.await(login.token)) {
      is Success -> {
        onAccounts(response.value)
        Idle
      }
      is Failed -> {
        failed(response.error)
      }
    }
  }

  private fun loadBanks(country: String) {
    banksJob?.cancel()
    mutableState.update { it.copy(banks = Loading) }
    banksJob = scope.launch {
      val banks =
        try {
          when (val response = provider.banks(country)) {
            is Success -> {
              LoginBanks.Loaded(response.value.toImmutableList())
            }
            is Failed -> {
              logcat.w { "Listing ${provider.name} banks in $country failed: ${response.error}" }
              LoginBanks.Failure(response.error.cause())
            }
          }
        } catch (e: CancellationException) {
          throw e
        } catch (e: Exception) {
          logcat.e(e) { "Failed listing ${provider.name} banks in $country" }
          LoginBanks.Failure(e.requireMessage())
        }
      mutableState.update { it.copy(banks = banks) }
    }
  }

  private fun setStatus(status: BankLoginStatus) = mutableState.update { it.copy(status = status) }

  private fun failed(error: Failure): BankLoginStatus {
    logcat.w { "${provider.name} login failed: $error" }
    val isTimeout = error is ProviderError && error.errorCode == ProviderError.TIMED_OUT
    return BankLoginStatus.Failed(error.cause().takeUnless { isTimeout }, isTimeout)
  }

  private companion object {
    // The user's own country if the provider supports it, as upstream guesses from the browser
    fun defaultCountry(countries: List<String>): String =
      Locale.getDefault().country.takeIf { it in countries } ?: "GB"
  }
}
