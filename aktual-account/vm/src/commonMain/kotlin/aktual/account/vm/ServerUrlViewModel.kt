package aktual.account.vm

import aktual.api.client.AccountApi
import aktual.api.model.account.NeedsBootstrapResponse
import aktual.budget.demo.DemoBudget
import aktual.core.model.AktualVersions
import aktual.core.model.AktualVersionsStateHolder
import aktual.core.model.BuildConfig
import aktual.core.model.Protocol
import aktual.core.model.ServerUrl
import aktual.core.utils.LocalNetworkPermission
import aktual.core.utils.isMissingFor
import aktual.core.utils.runCatchingCancellable
import aktual.di.AppScope
import aktual.di.RunLevelController
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.log
import aktual.prefs.AppPreferences
import aktual.prefs.delete
import alakazam.kotlin.CoroutineContexts
import alakazam.kotlin.ResettableStateFlow
import alakazam.kotlin.collectFlow
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Stable
@ViewModelKey
@ContributesIntoMap(AppScope::class)
class ServerUrlViewModel
internal constructor(
  private val contexts: CoroutineContexts,
  private val preferences: AppPreferences,
  private val runLevelController: RunLevelController,
  private val demoBudget: DemoBudget,
  private val localNetworkPermission: LocalNetworkPermission,
  versionsStateHolder: AktualVersionsStateHolder,
  buildConfig: BuildConfig,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  private val mutableIsLoading = ResettableStateFlow(value = false)
  private val mutableBaseUrl = MutableStateFlow(buildConfig.defaultServerUrl?.baseUrl.orEmpty())
  private val mutableProtocol = MutableStateFlow(buildConfig.defaultServerUrl?.protocol ?: Https)
  private val mutableConfirmResult = ResettableStateFlow<ConfirmResult?>(value = null)
  private val mutableNavDestination = Channel<NavDestination>()
  private val mutablePermissionRequests = Channel<Unit>(capacity = Channel.CONFLATED)

  // The URL confirmed while waiting for a permission result. Null in a new VM after process death
  private val pendingUrl = MutableStateFlow<ServerUrl?>(value = null)
  private var permissionCheckJob: Job? = null

  val versions: StateFlow<AktualVersions> = versionsStateHolder.asStateFlow()

  val baseUrl: StateFlow<String> = mutableBaseUrl.asStateFlow()
  val protocol: StateFlow<Protocol> = mutableProtocol.asStateFlow()
  val isLoading: StateFlow<Boolean> = mutableIsLoading.asStateFlow()

  val isEnabled: StateFlow<Boolean> =
    baseUrl.map { it.isNotBlank() }.stateIn(viewModelScope, Eagerly, initialValue = false)

  val navDestination: ReceiveChannel<NavDestination> = mutableNavDestination

  // Asks the UI to request local network permission, then call onLocalNetworkPermissionResult
  val localNetworkPermissionRequests: ReceiveChannel<Unit> = mutablePermissionRequests

  val error: StateFlow<ServerUrlError?> =
    viewModelScope.launchMolecule(Immediate) {
      val confirmResult by mutableConfirmResult.collectAsState()
      when (val result = confirmResult) {
        is Failed -> ServerUrlError.Failed(message = "Failed: ${result.reason}")
        LocalNetworkBlocked -> ServerUrlError.LocalNetworkBlocked
        is Succeeded,
        null -> null
      }
    }

  init {
    viewModelScope.launch {
      val savedUrl = preferences.serverUrl.get()
      if (savedUrl != null) {
        mutableBaseUrl.update { savedUrl.baseUrl }
        mutableProtocol.update { savedUrl.protocol }
      }

      preferences.token.delete()
    }

    viewModelScope.collectFlow(mutableConfirmResult) { result ->
      val navDestination = result.navDestination()
      if (navDestination != null) mutableNavDestination.send(navDestination)
    }
  }

  override fun onCleared() {
    clearState()
  }

  fun clearState() {
    // Drop any unanswered permission request, so it doesn't fire when the screen comes back
    permissionCheckJob?.cancel()
    pendingUrl.update { null }
    mutablePermissionRequests.tryReceive()
    mutableIsLoading.reset()
    mutableConfirmResult.reset()
  }

  fun onEnterUrl(url: String) {
    log.v { "onUrlEntered $url" }
    mutableBaseUrl.update { url }
    mutableConfirmResult.update { null }
  }

  fun onClickTryDemo() {
    log.v { "onClickTryDemo" }
    mutableConfirmResult.reset()
    mutableIsLoading.update { true }
    viewModelScope.launch {
      runCatchingCancellable(
        attempt = {
          demoBudget.open()
          mutableNavDestination.send(ToDemo)
        },
        onFailure = { e ->
          log.e(e) { "Failed opening demo budget" }
          mutableConfirmResult.update { ConfirmResult.Failed(reason = e.requireMessage()) }
        },
        finally = { mutableIsLoading.update { false } },
      )
    }
  }

  fun onSelectProtocol(protocol: Protocol) {
    log.v { "onProtocolSelected $protocol" }
    mutableProtocol.update { protocol }
  }

  fun onClickConfirm() {
    log.v { "onClickConfirm" }
    mutableConfirmResult.reset()
    mutableIsLoading.update { true }
    val url = ServerUrl(mutableProtocol.value, mutableBaseUrl.value)
    permissionCheckJob = viewModelScope.launch {
      if (localNetworkPermission.isMissingFor(url)) {
        pendingUrl.update { url }
        mutablePermissionRequests.send(Unit)
      } else {
        // Not a child, so clearState doesn't cancel a connection in progress
        viewModelScope.launch { connect(url, isPermissionMissing = false) }
      }
    }
  }

  // Connects either way: the address check can be wrong, and a denial just means it'll fail
  fun onLocalNetworkPermissionResult(isGranted: Boolean) {
    log.v { "onLocalNetworkPermissionResult $isGranted" }
    val url = pendingUrl.getAndUpdate { null }
    if (url == null) {
      log.w { "No pending URL for permission result" }
      mutableIsLoading.update { false }
      return
    }
    viewModelScope.launch { connect(url, isPermissionMissing = true) }
  }

  // isPermissionMissing is what the check before connecting found, so a failure doesn't resolve
  // the host again
  private suspend fun connect(url: ServerUrl, isPermissionMissing: Boolean) {
    if (url != preferences.serverUrl.get()) {
      preferences.token.delete()
    }

    val serverChosenGraph = runLevelController.onServerChosen(url)
    preferences.serverUrl.set(url)
    val accountApi = serverChosenGraph[AccountApi::class]
    checkIfNeedsBootstrap(url, accountApi, isPermissionMissing)
    mutableIsLoading.update { false }
  }

  fun onClickAbout() {
    mutableNavDestination.trySend(ToAbout)
  }

  private suspend fun checkIfNeedsBootstrap(
    url: ServerUrl,
    accountApi: AccountApi,
    isPermissionMissing: Boolean,
  ) {
    runCatchingCancellable(
      attempt = {
        log.v { "checkIfNeedsBootstrap $url" }

        val response =
          try {
            withContext(contexts.io) { accountApi.needsBootstrap() }
          } catch (e: ResponseException) {
            log.e(e) { "HTTP failure checking bootstrap for $url" }
            e.response.body<NeedsBootstrapResponse.Failure>()
          }

        log.v { "response = $response" }
        val confirmResult =
          when (response) {
            is Success -> ConfirmResult.Succeeded(isBootstrapped = response.data.bootstrapped)
            is Failure -> ConfirmResult.Failed(reason = response.reason.reason)
          }
        mutableConfirmResult.update { confirmResult }
      },
      onFailure = { e ->
        log.w(e) { "Failed checking bootstrap for $url" }

        preferences.serverUrl.delete()

        val result =
          if (isPermissionMissing && !localNetworkPermission.isGranted()) {
            ConfirmResult.LocalNetworkBlocked
          } else {
            ConfirmResult.Failed(reason = e.requireMessage())
          }
        mutableConfirmResult.update { result }
      },
    )
  }

  private fun ConfirmResult?.navDestination(): NavDestination? =
    when (this) {
      null -> null
      is Failed -> null
      LocalNetworkBlocked -> null
      is Succeeded -> if (isBootstrapped) ToLogin else ToBootstrap
    }
}

internal sealed interface ConfirmResult {
  data class Failed(val reason: String) : ConfirmResult

  data class Succeeded(val isBootstrapped: Boolean) : ConfirmResult

  // Failed to connect to a local network address without the permission
  data object LocalNetworkBlocked : ConfirmResult
}

sealed interface ServerUrlError {
  data class Failed(val message: String) : ServerUrlError

  // Failed to connect to a local network address without the permission
  data object LocalNetworkBlocked : ServerUrlError
}

sealed interface NavDestination {
  data object ToBootstrap : NavDestination

  data object ToLogin : NavDestination

  data object ToAbout : NavDestination

  data object ToDemo : NavDestination
}
