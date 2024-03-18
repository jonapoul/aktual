package dev.jonpoulton.actual.serverurl.vm

import alakazam.android.core.IBuildConfig
import alakazam.kotlin.core.IODispatcher
import alakazam.kotlin.core.requireMessage
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jonpoulton.actual.api.client.ActualApis
import dev.jonpoulton.actual.api.client.ActualApisStateHolder
import dev.jonpoulton.actual.api.model.account.NeedsBootstrapResponse
import dev.jonpoulton.actual.core.model.Protocol
import dev.jonpoulton.actual.core.model.ServerUrl
import dev.jonpoulton.actual.core.model.serverUrlOrNull
import dev.jonpoulton.actual.serverurl.prefs.ServerUrlPreferences
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ServerUrlViewModel @Inject internal constructor(
  buildConfig: IBuildConfig,
  private val io: IODispatcher,
  private val apiStateHolder: ActualApisStateHolder,
  private val prefs: ServerUrlPreferences,
  private val serverVersionFetcher: ServerVersionFetcher,
) : ViewModel() {
  private val mutableIsLoading = MutableStateFlow(value = false)
  private val mutableBaseUrl = MutableStateFlow(value = "")
  private val mutableProtocol = MutableStateFlow(Protocol.Https)
  private val mutableConfirmResult = MutableStateFlow<ConfirmResult?>(value = null)

  val serverVersion: StateFlow<String?> = serverVersionFetcher.serverVersion
  val appVersion: StateFlow<String> = MutableStateFlow(buildConfig.versionName)

  val baseUrl: StateFlow<String> = mutableBaseUrl.asStateFlow()
  val protocol: StateFlow<Protocol> = mutableProtocol.asStateFlow()
  val isLoading: StateFlow<Boolean> = mutableIsLoading.asStateFlow()

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
      serverVersionFetcher.startFetching()
    }

    viewModelScope.launch {
      val savedUrl = prefs.url.get()
      if (savedUrl != null) {
        mutableBaseUrl.update { savedUrl.baseUrl }
        mutableProtocol.update { savedUrl.protocol }
      }
    }
  }

  fun onUrlEntered(url: String) {
    Timber.v("onUrlEntered $url")
    mutableBaseUrl.update { url }
    mutableConfirmResult.update { null }
  }

  fun onProtocolSelected(protocol: Protocol) {
    Timber.v("onProtocolSelected $protocol")
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
    Timber.v("checkIfNeedsBootstrap $url")
    val apis = apiStateHolder.state
      .filterNotNull()
      .filter { it.serverUrl == url }
      .first()

    Timber.v("apis=$apis")
    val response = withContext(io) { apis.account.needsBootstrap() }
    Timber.v("response=$response")

    val confirmResult = when (response) {
      is NeedsBootstrapResponse.Ok -> {
        ConfirmResult.Succeeded(isBootstrapped = response.data.bootstrapped)
      }

      is NeedsBootstrapResponse.Error -> {
        ConfirmResult.Failed(reason = response.reason)
      }
    }
    mutableConfirmResult.update { confirmResult }
  } catch (e: Exception) {
    Timber.w(e, "Failed checking bootstrap for $url")
    mutableConfirmResult.update {
      ConfirmResult.Failed(reason = e.requireMessage())
    }
  }
}
