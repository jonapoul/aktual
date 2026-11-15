package aktual.core.ui

import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.runtime.Composable

interface AnimatedFlowRowScope {
  /**
   * Adds one item to the row. Pass a stable [key] when items can be added, removed or reordered, so
   * each one keeps its own animation state. A [weight] shares the row's spare width like
   * [FlowRowScope.weight], stretching the content to fill it.
   */
  fun item(key: Any? = null, weight: Float? = null, content: @Composable () -> Unit)
}
