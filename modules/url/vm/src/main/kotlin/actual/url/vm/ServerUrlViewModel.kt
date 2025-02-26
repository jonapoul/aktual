package actual.url.vm

import actual.account.login.domain.LoginPreferences
import actual.api.client.ActualApisStateHolder
import actual.api.client.adapted
import actual.api.model.account.NeedsBootstrapResponse
import actual.core.colorscheme.ColorSchemePreferences
import actual.core.colorscheme.ColorSchemeType
import actual.core.versions.ActualVersions
import actual.core.versions.ActualVersionsStateHolder
import actual.log.Logger
import actual.url.model.Protocol
import actual.url.model.ServerUrl
import actual.url.prefs.ServerUrlPreferences
import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.ResettableStateFlow
import alakazam.kotlin.core.collectFlow
import alakazam.kotlin.core.requireMessage
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode.Immediate
import app.cash.molecule.launchMolecule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ServerUrlViewModel @Inject internal constructor(
  private val contexts: CoroutineContexts,
  private val apiStateHolder: ActualApisStateHolder,
  private val serverUrlPreferences: ServerUrlPreferences,
  private val loginPreferences: LoginPreferences,
  colorSchemePreferences: ColorSchemePreferences,
  versionsStateHolder: ActualVersionsStateHolder,
  urlProvider: ServerUrl.Provider,
) : ViewModel() {
  private val defaultUrl = urlProvider.default()

  private val mutableIsLoading = ResettableStateFlow(value = false)
  private val mutableBaseUrl = MutableStateFlow(defaultUrl?.baseUrl.orEmpty())
  private val mutableProtocol = MutableStateFlow(defaultUrl?.protocol ?: Protocol.Https)
  private val mutableConfirmResult = ResettableStateFlow<ConfirmResult?>(value = null)
  private val mutableNavDestination = Channel<NavDestination>()

  val versions: StateFlow<ActualVersions> = versionsStateHolder.asStateFlow()

  val baseUrl: StateFlow<String> = mutableBaseUrl.asStateFlow()
  val protocol: StateFlow<Protocol> = mutableProtocol.asStateFlow()
  val isLoading: StateFlow<Boolean> = mutableIsLoading.asStateFlow()
  val themeType: StateFlow<ColorSchemeType> = colorSchemePreferences.stateFlow(viewModelScope)

  val isEnabled: StateFlow<Boolean> = baseUrl
    .map { it.isNotBlank() }
    .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = false)

  val navDestination: ReceiveChannel<NavDestination> = mutableNavDestination

  val errorMessage: StateFlow<String?> = viewModelScope.launchMolecule(Immediate) {
    val confirmResult by mutableConfirmResult.collectAsState()
    val result = confirmResult
    if (result is ConfirmResult.Failed) {
      "Failed: ${result.reason}"
    } else {
      null
    }
  }

  init {
    viewModelScope.launch {
      val savedUrl = serverUrlPreferences.url.get()
      if (savedUrl != null) {
        mutableBaseUrl.update { savedUrl.baseUrl }
        mutableProtocol.update { savedUrl.protocol }
      }

      // Also clear any previous login state
      loginPreferences.token.deleteAndCommit()
    }

    viewModelScope.collectFlow(mutableConfirmResult) { result ->
      val navDestination = result.navDestination()
      if (navDestination != null) mutableNavDestination.send(navDestination)
    }
  }

  fun clearState() {
    mutableIsLoading.reset()
    mutableConfirmResult.reset()
  }

  fun onEnterUrl(url: String) {
    Logger.v("onUrlEntered %s", url)
    mutableBaseUrl.update { url }
    mutableConfirmResult.update { null }
  }

  fun onUseDemoServer() {
    Logger.v("onUseDemoServer")
    val demo = ServerUrl.Demo
    mutableBaseUrl.update { demo.baseUrl }
    mutableProtocol.update { demo.protocol }
    mutableConfirmResult.update { null }
  }

  fun onSelectProtocol(protocol: Protocol) {
    Logger.v("onProtocolSelected %s", protocol)
    mutableProtocol.update { protocol }
  }

  fun onClickConfirm() {
    Logger.v("onClickConfirm")
    mutableIsLoading.update { true }
    viewModelScope.launch {
      val protocol = mutableProtocol.value
      val baseUrl = mutableBaseUrl.value
      val url = ServerUrl(protocol, baseUrl)
      val previousUrl = serverUrlPreferences.url.get()

      if (url != previousUrl) {
        // saving a new URL, so the existing token and API objects are invalidated
        apiStateHolder.update { null }
        loginPreferences.token.deleteAndCommit()
      }

      serverUrlPreferences.url.set(url)
      checkIfNeedsBootstrap(url)
      mutableIsLoading.update { false }
    }
  }

  fun onClickBack() {
    mutableNavDestination.trySend(NavDestination.Back)
  }

  private suspend fun checkIfNeedsBootstrap(url: ServerUrl) = try {
    Logger.v("checkIfNeedsBootstrap %s", url)
    val apis = apiStateHolder
      .filterNotNull()
      .filter { it.serverUrl == url }
      .first()

    Logger.v("apis = %s", apis)
    val response = withContext(contexts.io) {
      apis.account
        .needsBootstrap()
        .adapted(NeedsBootstrapResponse.Failure.serializer())
    }
    Logger.v("response = %s", response)

    val confirmResult = when (val body = response.body) {
      is NeedsBootstrapResponse.Success -> ConfirmResult.Succeeded(isBootstrapped = body.data.bootstrapped)
      is NeedsBootstrapResponse.Failure -> ConfirmResult.Failed(reason = body.reason.reason)
    }
    mutableConfirmResult.update { confirmResult }
  } catch (e: CancellationException) {
    throw e
  } catch (e: Exception) {
    Logger.w(e, "Failed checking bootstrap for %s", url)

    // hit an error, we can't use this URL?
    serverUrlPreferences.url.deleteAndCommit()

    mutableConfirmResult.update { ConfirmResult.Failed(reason = e.requireMessage()) }
  }

  private fun ConfirmResult?.navDestination(): NavDestination? = when (this) {
    null -> null
    is ConfirmResult.Failed -> null
    is ConfirmResult.Succeeded -> {
      if (isBootstrapped) {
        NavDestination.ToLogin
      } else {
        NavDestination.ToBootstrap
      }
    }
  }
}
