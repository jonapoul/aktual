package actual.serverurl.vm

import actual.api.client.ActualApisStateHolder
import actual.api.model.account.NeedsBootstrapResponse
import actual.core.coroutines.CoroutineContexts
import actual.core.coroutines.ResettableStateFlow
import actual.core.model.ActualVersions
import actual.core.model.Protocol
import actual.core.model.ServerUrl
import actual.core.state.ActualVersionsStateHolder
import actual.serverurl.prefs.ServerUrlPreferences
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
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class ServerUrlViewModel @Inject internal constructor(
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
    Timber.v("onUrlEntered %s", url)
    mutableBaseUrl.update { url }
    mutableConfirmResult.update { null }
  }

  fun onSelectProtocol(protocol: Protocol) {
    Timber.v("onProtocolSelected %s", protocol)
    mutableProtocol.update { protocol }
  }

  fun onClickConfirm() {
    Timber.v("onClickConfirm")
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
    Timber.v("checkIfNeedsBootstrap %s", url)
    val apis = apiStateHolder
      .filterNotNull()
      .filter { it.serverUrl == url }
      .first()

    Timber.v("apis = %s", apis)
    val response = withContext(contexts.io) { apis.account.needsBootstrap() }
    Timber.v("response = %s", response)

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
    Timber.w(e, "Failed checking bootstrap for %s", url)
    mutableConfirmResult.update {
      ConfirmResult.Failed(reason = e.requireMessage())
    }
  }
}
