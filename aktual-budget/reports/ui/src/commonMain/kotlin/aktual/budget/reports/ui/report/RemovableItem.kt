package aktual.budget.reports.ui.report

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.first

// Expands a newly added item in, and shrinks an item out before removing it, so the items below
// slide to make room. Put any spacing inside the content, or it jumps at the end
@Composable
internal fun RemovableItem(
  isNew: Boolean,
  onRemove: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable (remove: () -> Unit) -> Unit,
) {
  val visibility = remember { MutableTransitionState(!isNew).apply { targetState = true } }
  val currentOnRemove by rememberUpdatedState(onRemove)

  LaunchedEffect(visibility) {
    snapshotFlow { visibility.isIdle && !visibility.targetState }.first { it }
    currentOnRemove()
  }

  AnimatedVisibility(
    modifier = modifier,
    visibleState = visibility,
    enter = expandVertically() + fadeIn(),
    exit = shrinkVertically() + fadeOut(),
  ) {
    content { visibility.targetState = false }
  }
}

// Stable keys for a list without ids, so each row keeps its state when the ones above it go
internal class ItemKeys(private val initialSize: Int) {
  private var next = 0
  private val keys = MutableList(initialSize) { next++ }

  operator fun get(index: Int): Int = keys.getOrElse(index) { -index - 1 }

  fun isNew(index: Int): Boolean = this[index] >= initialSize

  fun added() {
    keys += next++
  }

  fun removed(index: Int) {
    if (index in keys.indices) keys.removeAt(index)
  }
}
