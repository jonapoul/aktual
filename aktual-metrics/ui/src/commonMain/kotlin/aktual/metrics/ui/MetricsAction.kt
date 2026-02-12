package aktual.metrics.ui

import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface MetricsAction {
  data object NavBack : MetricsAction

  data object Refresh : MetricsAction
}
