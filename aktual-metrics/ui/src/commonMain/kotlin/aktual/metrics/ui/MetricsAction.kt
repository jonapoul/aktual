package aktual.metrics.ui

import androidx.compose.runtime.Immutable

@Immutable internal sealed interface MetricsAction

internal data object NavBack : MetricsAction

internal data object Refresh : MetricsAction

@Immutable
internal fun interface MetricsActionHandler {
  operator fun invoke(action: MetricsAction)
}
