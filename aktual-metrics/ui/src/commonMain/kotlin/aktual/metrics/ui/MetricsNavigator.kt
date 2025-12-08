package aktual.metrics.ui

import androidx.compose.runtime.Immutable

@Immutable
fun interface MetricsNavigator {
  fun back(): Boolean
}
