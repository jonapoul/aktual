package aktual.account.vm

import aktual.api.client.AktualApisStateHolder
import aktual.api.model.account.NeedsBootstrapResponse
import aktual.core.model.AktualVersions
import aktual.core.model.AktualVersionsStateHolder
import aktual.core.model.BuildConfig
import aktual.core.model.Protocol
import aktual.core.model.ServerUrl
import aktual.prefs.AppGlobalPreferences
import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.ResettableStateFlow
import alakazam.kotlin.core.collectFlow
import alakazam.kotlin.core.requireMessage
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode.Immediate
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
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
import logcat.logcat

@Inject
@ViewModelKey(ServerUrlViewModel::class)
@ContributesIntoMap(AppScope::class)
class ServerUrlViewModel internal constructor(
  private val contexts: CoroutineContexts,
  private val apiStateHolder: AktualApisStateHolder,
  private val preferences: AppGlobalPreferences,
  versionsStateHolder: AktualVersionsStateHolder,
  buildConfig: BuildConfig,
) : ViewModel() {
  private val mutableIsLoading = ResettableStateFlow(value = false)
  private val mutableBaseUrl = MutableStateFlow(buildConfig.defaultServerUrl?.baseUrl.orEmpty())
  private val mutableProtocol = MutableStateFlow(buildConfig.defaultServerUrl?.protocol ?: Protocol.Https)
  private val mutableConfirmResult = ResettableStateFlow<ConfirmResult?>(value = null)
  private val mutableNavDestination = Channel<NavDestination>()

  val versions: StateFlow<AktualVersions> = versionsStateHolder.asStateFlow()

  val baseUrl: StateFlow<String> = mutableBaseUrl.asStateFlow()
  val protocol: StateFlow<Protocol> = mutableProtocol.asStateFlow()
  val isLoading: StateFlow<Boolean> = mutableIsLoading.asStateFlow()

  val isEnabled: StateFlow<Boolean> = baseUrl
    .map { it.isNotBlank() }
    .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = false)

  val navDestination: ReceiveChannel<NavDestination> = mutableNavDestination

  val errorMessage: StateFlow<String?> = viewModelScope.launchMolecule(Immediate) {
    val confirmResult by mutableConfirmResult.collectAsStateWithLifecycle()
    val result = confirmResult
    if (result is ConfirmResult.Failed) {
      "Failed: ${result.reason}"
    } else {
      null
    }
  }

  init {
    viewModelScope.launch {
      val savedUrl = preferences.serverUrl.get()
      if (savedUrl != null) {
        mutableBaseUrl.update { savedUrl.baseUrl }
        mutableProtocol.update { savedUrl.protocol }
      }

      // Also clear any previous login state
      preferences.loginToken.deleteAndCommit()
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
    logcat.v { "onUrlEntered $url" }
    mutableBaseUrl.update { url }
    mutableConfirmResult.update { null }
  }

  fun onUseDemoServer() {
    logcat.v { "onUseDemoServer" }
    val demo = ServerUrl.Demo
    mutableBaseUrl.update { demo.baseUrl }
    mutableProtocol.update { demo.protocol }
    mutableConfirmResult.update { null }
  }

  fun onSelectProtocol(protocol: Protocol) {
    logcat.v { "onProtocolSelected $protocol" }
    mutableProtocol.update { protocol }
  }

  fun onClickConfirm() {
    logcat.v { "onClickConfirm" }
    mutableIsLoading.update { true }
    viewModelScope.launch {
      val protocol = mutableProtocol.value
      val baseUrl = mutableBaseUrl.value
      val url = ServerUrl(protocol, baseUrl)
      val previousUrl = preferences.serverUrl.get()

      if (url != previousUrl) {
        // saving a new URL, so the existing token and API objects are invalidated
        apiStateHolder.update { null }
        preferences.loginToken.deleteAndCommit()
      }

      preferences.serverUrl.set(url)
      checkIfNeedsBootstrap(url)
      mutableIsLoading.update { false }
    }
  }

  fun onClickBack() {
    mutableNavDestination.trySend(NavDestination.Back)
  }

  fun onClickAbout() {
    mutableNavDestination.trySend(NavDestination.ToAbout)
  }

  private suspend fun checkIfNeedsBootstrap(url: ServerUrl) = try {
    logcat.v { "checkIfNeedsBootstrap $url" }
    val apis = apiStateHolder
      .filterNotNull()
      .filter { it.serverUrl == url }
      .first()

    logcat.v { "apis = $apis" }
    val response = try {
      withContext(contexts.io) { apis.account.needsBootstrap() }
    } catch (e: ResponseException) {
      logcat.e(e) { "HTTP failure checking bootstrap for $url" }
      e.response.body<NeedsBootstrapResponse.Failure>()
    }

    logcat.v { "response = $response" }
    val confirmResult = when (response) {
      is NeedsBootstrapResponse.Success -> ConfirmResult.Succeeded(isBootstrapped = response.data.bootstrapped)
      is NeedsBootstrapResponse.Failure -> ConfirmResult.Failed(reason = response.reason.reason)
    }
    mutableConfirmResult.update { confirmResult }
  } catch (e: CancellationException) {
    throw e
  } catch (e: Exception) {
    logcat.w(e) { "Failed checking bootstrap for $url" }

    // hit an error, we can't use this URL?
    preferences.serverUrl.deleteAndCommit()

    mutableConfirmResult.update { ConfirmResult.Failed(reason = e.requireMessage()) }
  }

  private fun ConfirmResult?.navDestination(): NavDestination? = when (this) {
    null -> {
      null
    }

    is ConfirmResult.Failed -> {
      null
    }

    is ConfirmResult.Succeeded -> {
      if (isBootstrapped) {
        NavDestination.ToLogin
      } else {
        NavDestination.ToBootstrap
      }
    }
  }
}

@Immutable
internal sealed interface ConfirmResult {
  data class Failed(
    val reason: String,
  ) : ConfirmResult

  data class Succeeded(
    val isBootstrapped: Boolean,
  ) : ConfirmResult
}

@Immutable
sealed interface NavDestination {
  data object Back : NavDestination
  data object ToBootstrap : NavDestination
  data object ToLogin : NavDestination
  data object ToAbout : NavDestination
}
