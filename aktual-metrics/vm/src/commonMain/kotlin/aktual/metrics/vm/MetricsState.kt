package aktual.metrics.vm

import aktual.api.model.metrics.GetMetricsResponse.Memory
import androidx.compose.runtime.Immutable
import kotlin.time.Duration
import kotlin.time.Instant

@Immutable
sealed interface MetricsState {
  data object Loading : MetricsState

  data object Disconnected : MetricsState

  data class Failure(val cause: String) : MetricsState

  data class Success(val memory: Memory, val uptime: Duration, val lastUpdate: Instant) :
    MetricsState
}
