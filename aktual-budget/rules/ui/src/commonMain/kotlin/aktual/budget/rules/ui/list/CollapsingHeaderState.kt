package aktual.budget.rules.ui.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource

// Owns the collapse offset + measured height of the [CollapsingHeader] and the nested-scroll
// connection that drives them, mirroring Material's own TopAppBarState/TopAppBarScrollBehavior.
@Stable
internal class CollapsingHeaderState {
  // Current vertical collapse, between -height (fully collapsed) and 0 (fully expanded).
  var offset by mutableFloatStateOf(0f)
    private set

  // Full (expanded) height of the header in px, written from the header's layout pass.
  var height by mutableFloatStateOf(0f)

  // True while the header is anything less than fully expanded.
  val isCollapsing: Boolean
    get() = offset < 0f

  // Fully opaque when expanded, fading to transparent as it collapses.
  val alpha: Float
    get() = if (height > 0f) (1f + offset / height).coerceIn(0f, 1f) else 1f

  val nestedScrollConnection =
    object : NestedScrollConnection {
      override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (height <= 0f) return Offset.Zero
        val previous = offset
        val new = (previous + available.y).coerceIn(-height, 0f)
        offset = new
        return Offset(x = 0f, y = new - previous)
      }
    }
}

@Composable
internal fun rememberCollapsingHeaderState(): CollapsingHeaderState = remember {
  CollapsingHeaderState()
}
