package aktual.about.ui.logs

import aktual.about.vm.logs.LiveState.Following
import aktual.about.vm.logs.LiveState.None
import aktual.about.vm.logs.LogEntriesState
import aktual.core.logging.LogLine
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.saveable.rememberSaveable

/**
 * Log lists use `reverseLayout`, so the newest line is item 0 at the bottom. Appending a line then
 * slides the keyed rows above it up instead of recomposing every visible row.
 */
@Composable
internal fun rememberLogListState(state: LogEntriesState.Loaded): LazyListState {
  val listState =
    rememberSaveable(state.date, saver = LazyListState.Saver) {
      // Older days open at their first line, today at its newest
      LazyListState(firstVisibleItemIndex = if (state.live == None) state.lines.size else 0)
    }
  SideEffect(state.lines.size) {
    if (state.live == Following) listState.requestScrollToItem(0)
  }
  return listState
}

/** Emits [lines] newest first, keyed by their index so the keys survive new lines being added. */
internal inline fun LazyListScope.reversedLogLines(
  lines: List<LogLine>,
  crossinline content: @Composable LazyItemScope.(index: Int, line: LogLine) -> Unit,
) =
  items(count = lines.size, key = { lines.lastIndex - it }) { reversed ->
    val index = lines.lastIndex - reversed
    content(index, lines[index])
  }
