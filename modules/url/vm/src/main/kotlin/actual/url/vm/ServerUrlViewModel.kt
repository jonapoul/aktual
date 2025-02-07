package actual.url.vm

import actual.api.client.ActualApisStateHolder
import actual.api.model.account.NeedsBootstrapResponse
import actual.core.coroutines.CoroutineContexts
import actual.core.coroutines.ResettableStateFlow
import actual.core.versions.ActualVersions
import actual.core.versions.ActualVersionsStateHolder
import actual.log.Logger
import actual.url.model.Protocol
import actual.url.model.ServerUrl
import actual.url.prefs.ServerUrlPreferences
import alakazam.kotlin.core.requireMessage
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
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
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class ServerUrlViewModel @Inject internal constructor(
  private val logger: Logger,
  private val contexts: CoroutineContexts,
  private val apiStateHolder: ActualApisStateHolder,
  private val prefs: ServerUrlPreferences,
  versionsStateHolder: ActualVersionsStateHolder,
) : ViewModel() {
  private val mutableIsLoading = ResettableStateFlow(value = false)
  private val mutableBaseUrl = MutableStateFlow(value = "")
  private val mutableProtocol = MutableStateFlow(Protocol.Https)
  private val mutableConfirmResult = ResettableStateFlow<ConfirmResult?>(value = null)

  val versions: StateFlow<ActualVersions> = versionsStateHolder.asStateFlow()

  val baseUrl: StateFlow<String> = mutableBaseUrl.asStateFlow()
  val protocol: StateFlow<Protocol> = mutableProtocol.asStateFlow()
  val isLoading: StateFlow<Boolean> = mutableIsLoading.asStateFlow()

  val isEnabled: StateFlow<Boolean> = baseUrl
    .map { it.isNotBlank() }
    .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = false)

  val shouldNavigate: Flow<ShouldNavigate?> = mutableConfirmResult.map { value ->
    if (value is ConfirmResult.Succeeded) {
      if (value.isBootstrapped) {
        ShouldNavigate.ToLogin
      } else {
        ShouldNavigate.ToBootstrap
      }
    } else {
      null
    }
  }

  val errorMessage: Flow<String?> = mutableConfirmResult.map { value ->
    if (value is ConfirmResult.Failed) "Failed: ${value.reason}" else null
  }

  val protocols: ImmutableList<String> = Protocol.entries.map { it.toString() }.toImmutableList()

  init {
    viewModelScope.launch {
      val savedUrl = prefs.url.get()
      if (savedUrl != null) {
        mutableBaseUrl.update { savedUrl.baseUrl }
        mutableProtocol.update { savedUrl.protocol }
      }
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
      prefs.url.set(url)
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
    val response = withContext(contexts.io) { apis.account.needsBootstrap() }
    logger.v("response = %s", response)

    val confirmResult = when (response) {
      is NeedsBootstrapResponse.Ok -> {
        ConfirmResult.Succeeded(isBootstrapped = response.data.bootstrapped)
      }

      is NeedsBootstrapResponse.Error -> {
        ConfirmResult.Failed(reason = response.reason)
      }
    }
    mutableConfirmResult.update { confirmResult }
  } catch (e: CancellationException) {
    throw e
  } catch (e: Exception) {
    logger.w(e, "Failed checking bootstrap for %s", url)

    // hit an error, we can't use this URL?
    prefs.url.deleteAndCommit()

    mutableConfirmResult.update { ConfirmResult.Failed(reason = e.requireMessage()) }
  }
}
