package actual.url.vm

import actual.account.login.domain.LoginPreferences
import actual.api.client.ActualApisStateHolder
import actual.api.client.ActualJson
import actual.api.core.adapted
import actual.api.model.account.NeedsBootstrapResponse
import actual.core.colorscheme.ColorSchemePreferences
import actual.core.colorscheme.ColorSchemeType
import actual.core.versions.ActualVersions
import actual.core.versions.ActualVersionsStateHolder
import actual.url.model.Protocol
import actual.url.model.ServerUrl
import actual.url.prefs.ServerUrlPreferences
import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.Logger
import alakazam.kotlin.core.ResettableStateFlow
import alakazam.kotlin.core.requireMessage
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode.Immediate
import app.cash.molecule.launchMolecule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
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
  private val logger: Logger,
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

  val versions: StateFlow<ActualVersions> = versionsStateHolder.asStateFlow()

  val baseUrl: StateFlow<String> = mutableBaseUrl.asStateFlow()
  val protocol: StateFlow<Protocol> = mutableProtocol.asStateFlow()
  val isLoading: StateFlow<Boolean> = mutableIsLoading.asStateFlow()
  val themeType: StateFlow<ColorSchemeType> = colorSchemePreferences.stateFlow(viewModelScope)

  val isEnabled: StateFlow<Boolean> = baseUrl
    .map { it.isNotBlank() }
    .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = false)

  val shouldNavigate: StateFlow<ShouldNavigate?> = viewModelScope.launchMolecule(Immediate) {
    val confirmResult by mutableConfirmResult.collectAsState()
    when (val result = confirmResult) {
      is ConfirmResult.Succeeded ->
        if (result.isBootstrapped) ShouldNavigate.ToLogin else ShouldNavigate.ToBootstrap

      else -> null
    }
  }

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
  }

  fun clearState() {
    mutableIsLoading.reset()
    mutableConfirmResult.reset()
  }

  fun onEnterUrl(url: String) {
    logger.v("onUrlEntered %s", url)
    mutableBaseUrl.update { url }
    mutableConfirmResult.update { null }
  }

  fun onSelectProtocol(protocol: Protocol) {
    logger.v("onProtocolSelected %s", protocol)
    mutableProtocol.update { protocol }
  }

  fun onClickConfirm() {
    logger.v("onClickConfirm")
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

  private suspend fun checkIfNeedsBootstrap(url: ServerUrl) = try {
    logger.v("checkIfNeedsBootstrap %s", url)
    val apis = apiStateHolder
      .filterNotNull()
      .filter { it.serverUrl == url }
      .first()

    logger.v("apis = %s", apis)
    val response = withContext(contexts.io) {
      apis.account
        .needsBootstrap()
        .adapted(ActualJson, NeedsBootstrapResponse.Failure.serializer())
    }
    logger.v("response = %s", response)

    val confirmResult = when (val body = response.body) {
      is NeedsBootstrapResponse.Success -> ConfirmResult.Succeeded(isBootstrapped = body.data.bootstrapped)
      is NeedsBootstrapResponse.Failure -> ConfirmResult.Failed(reason = body.reason.reason)
    }
    mutableConfirmResult.update { confirmResult }
  } catch (e: CancellationException) {
    throw e
  } catch (e: Exception) {
    logger.w(e, "Failed checking bootstrap for %s", url)

    // hit an error, we can't use this URL?
    serverUrlPreferences.url.deleteAndCommit()

    mutableConfirmResult.update { ConfirmResult.Failed(reason = e.requireMessage()) }
  }
}
