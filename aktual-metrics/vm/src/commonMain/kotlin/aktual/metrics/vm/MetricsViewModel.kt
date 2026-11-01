package aktual.metrics.vm

import aktual.api.client.MetricsApi
import aktual.core.runCatchingCancellable
import aktual.di.LoggedInScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.metrics.vm.MetricsState.Failure
import alakazam.kotlin.CoroutineContexts
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import java.io.IOException
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import logcat.logcat

@Stable
@ViewModelKey
@ContributesIntoMap(LoggedInScope::class)
class MetricsViewModel
internal constructor(
  private val metricsApi: MetricsApi,
  private val contexts: CoroutineContexts,
  private val clock: Clock,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  private val fetchCount = MutableStateFlow(value = 0)
  private val mutableState = MutableStateFlow<MetricsState>(Loading)

  val state: StateFlow<MetricsState> = mutableState.asStateFlow()

  init {
    viewModelScope.launch { fetchCount.collectLatest { counter -> fetchMetrics(counter) } }
  }

  fun refresh() {
    fetchCount.update { it + 1 }
  }

  private suspend fun fetchMetrics(counter: Int) {
    logcat.d { "fetchMetrics counter=$counter" }
    mutableState.update { Loading }
    runCatchingCancellable(
      attempt = {
        val response = withContext(contexts.io) { metricsApi.getMetrics() }
        logcat.v { "fetchMetrics success: $response" }
        val state = MetricsState.Success(response.memory, response.uptime, lastUpdate = clock.now())
        mutableState.update { state }
      },
      onFailure = { e ->
        when (e) {
          is IOException -> {
            logcat.e(e) { "Failed I/O when fetching metrics" }
            mutableState.update { Disconnected }
          }
          is SerializationException -> {
            logcat.e(e) { "Failed deserializing metrics response" }
            mutableState.update { Failure(e.requireMessage()) }
          }
          else -> {
            logcat.e(e) { "Failed fetching metrics" }
            mutableState.update { Failure(e.requireMessage()) }
          }
        }
      },
    )
  }
}
