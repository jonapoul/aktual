package aktual.budget.budgeting.ui

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.zIndex

// Lists here scroll under the top bar, but sticky headers pin to the very top of a list, ignoring
// its content padding. This shifts each header down so it sticks just below the bar instead
@Stable
internal class StuckHeaders(
  private val listState: LazyListState,
  private val topBar: Dp,
) {
  fun modifier(key: Any): Modifier =
    Modifier.zIndex(1f).graphicsLayer {
      translationY = listState.layoutInfo.shift(key, topBar.roundToPx())
    }

  private fun LazyListLayoutInfo.shift(key: Any, topBar: Int): Float {
    val item = visibleItemsInfo.fastFirstOrNull { it.key == key } ?: return 0f
    // Runs every frame, so one pass with no allocations
    var next: LazyListItemInfo? = null
    visibleItemsInfo.fastForEach { other ->
      val isLaterHeader = other.index > item.index && other.contentType == StickyHeader
      if (isLaterHeader && other.index < next?.index ?: MAX_VALUE) next = other
    }

    val top = top(item)
    var desired = maxOf(topBar, top)
    next?.let { desired = minOf(desired, maxOf(topBar, top(it)) - item.size) }
    return (desired - top).toFloat()
  }

  private fun LazyListLayoutInfo.top(item: LazyListItemInfo): Int =
    item.offset - viewportStartOffset

  companion object {
    const val StickyHeader = "sticky-header"
  }
}

// A sticky header that sticks below the top bar. The content gets the modifier that shifts it
internal fun LazyListScope.stuckHeader(
  key: Any,
  stuck: StuckHeaders,
  content: @Composable LazyItemScope.(Modifier) -> Unit,
) =
  stickyHeader(key = key, contentType = StuckHeaders.StickyHeader) { content(stuck.modifier(key)) }
