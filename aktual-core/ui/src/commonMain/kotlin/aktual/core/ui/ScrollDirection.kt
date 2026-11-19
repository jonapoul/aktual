package aktual.core.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// True until the content scrolls down, then again once it scrolls back up
@Composable
fun LazyListState.isScrollingUp(): Boolean {
  var isUp by remember(this) { mutableStateOf(true) }
  LaunchedEffect(this) {
    scrollsUp { firstVisibleItemIndex to firstVisibleItemScrollOffset }.collect { isUp = it }
  }
  return isUp
}

// True until the content scrolls down, then again once it scrolls back up
@Composable
fun ScrollState.isScrollingUp(): Boolean {
  var isUp by remember(this) { mutableStateOf(true) }
  LaunchedEffect(this) { scrollsUp { 0 to value }.collect { isUp = it } }
  return isUp
}

private fun scrollsUp(position: () -> Pair<Int, Int>): Flow<Boolean> = flow {
  var previous: Pair<Int, Int>? = null
  snapshotFlow(position).collect { current ->
    previous?.let { emit(compareValuesBy(current, it, { p -> p.first }, { p -> p.second }) < 0) }
    previous = current
  }
}
