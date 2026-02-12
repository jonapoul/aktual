package aktual.metrics.vm

import aktual.api.client.AktualApisStateHolder
import aktual.api.client.MetricsApi
import alakazam.kotlin.CoroutineContexts
import alakazam.kotlin.requireMessage
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlin.time.Clock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import logcat.logcat

@Inject
@ViewModelKey(MetricsViewModel::class)
@ContributesIntoMap(AppScope::class)
class MetricsViewModel
internal constructor(
  apisStateHolder: AktualApisStateHolder,
  private val contexts: CoroutineContexts,
  private val clock: Clock,
) : ViewModel() {
  private val fetchCount = MutableStateFlow(value = 0)
  private val mutableState = MutableStateFlow<MetricsState>(MetricsState.Loading)

  val state: StateFlow<MetricsState> = mutableState.asStateFlow()

  init {
    val metricApiFlow = apisStateHolder.map { it?.metrics }.distinctUntilChanged()
    viewModelScope.launch {
      combine(metricApiFlow, fetchCount, ::Pair).collectLatest { (metricsApi, counter) ->
        fetchMetrics(metricsApi, counter)
      }
    }
  }

  fun refresh() {
    fetchCount.update { it + 1 }
  }

  private suspend fun fetchMetrics(metricsApi: MetricsApi?, counter: Int) {
    logcat.d { "fetchMetrics counter=$counter" }
    if (metricsApi == null) {
      logcat.w { "Disconnected, can't fetch metrics" }
      mutableState.update { MetricsState.Disconnected }
      return
    }

    mutableState.update { MetricsState.Loading }
    try {
      val response = withContext(contexts.io) { metricsApi.getMetrics() }
      logcat.v { "fetchMetrics success: $response" }
      val state = MetricsState.Success(response.memory, response.uptime, lastUpdate = clock.now())
      mutableState.update { state }
    } catch (e: CancellationException) {
      throw e
    } catch (e: SerializationException) {
      logcat.e(e) { "Failed deserializing metrics response" }
      mutableState.update { MetricsState.Failure(e.requireMessage()) }
    } catch (e: Exception) {
      logcat.e(e) { "Failed fetching metrics" }
      mutableState.update { MetricsState.Failure(e.requireMessage()) }
    }
  }
}
