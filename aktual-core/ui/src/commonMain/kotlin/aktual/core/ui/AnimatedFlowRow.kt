package aktual.core.ui

import aktual.core.theme.Colors
import androidx.compose.animation.animateBounds
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach

/**
 * A [FlowRow] with 4.dp spacing that animates its items. Ones added after the first composition
 * fade and scale in, removed ones fade and scale out, the rest slide to their new place, and the
 * row's size changes smoothly.
 */
@Composable
fun AnimatedFlowRow(
  modifier: Modifier = Modifier,
  horizontalAlignment: Alignment.Horizontal = Alignment.Start,
  itemVerticalAlignment: Alignment.Vertical = Alignment.Top,
  maxItemsInEachRow: Int = MAX_VALUE,
  content: AnimatedFlowRowScope.() -> Unit,
) {
  val state = remember { AnimatedFlowRowState() }
  val entries = state.merge(AnimatedFlowRowScopeImpl().apply(content).entries)
  val isFirstComposition = state.isFirstComposition
  SideEffect { state.onComposed(entries) }

  LookaheadScope {
    FlowRow(
      modifier = modifier.animateContentSize(),
      horizontalArrangement = Arrangement.spacedBy(AnimatedFlowRowSpacing, horizontalAlignment),
      verticalArrangement = Arrangement.spacedBy(AnimatedFlowRowSpacing),
      itemVerticalAlignment = itemVerticalAlignment,
      maxItemsInEachRow = maxItemsInEachRow,
    ) {
      entries.fastForEach { entry ->
        key(entry.key) {
          val visibility = remember { Animatable(if (isFirstComposition) 1f else 0f) }
          LaunchedEffect(entry.isRemoved) {
            visibility.animateTo(if (entry.isRemoved) 0f else 1f)
            if (entry.isRemoved) state.onExited(entry.key)
          }
          Box(
            propagateMinConstraints = entry.weight != null,
            modifier =
              Modifier.then(if (entry.weight != null) Modifier.weight(entry.weight) else Modifier)
                .animateBounds(this@LookaheadScope)
                .graphicsLayer {
                  val value = visibility.value
                  val scale = HIDDEN_SCALE + (1f - HIDDEN_SCALE) * value
                  alpha = value
                  scaleX = scale
                  scaleY = scale
                },
          ) {
            entry.content()
          }
        }
      }
    }
  }
}

val AnimatedFlowRowSpacing = 4.dp

private const val HIDDEN_SCALE = 0.8f

private data class IndexKey(val index: Int)

private data class Entry(
  val key: Any,
  val weight: Float?,
  val isRemoved: Boolean,
  val content: @Composable () -> Unit,
)

private class AnimatedFlowRowScopeImpl : AnimatedFlowRowScope {
  val entries = mutableListOf<Entry>()

  override fun item(key: Any?, weight: Float?, content: @Composable () -> Unit) {
    entries += Entry(key ?: IndexKey(entries.size), weight, isRemoved = false, content)
  }
}

// Keeps removed items in their old place until their exit animation ends
private class AnimatedFlowRowState {
  var isFirstComposition = true
    private set

  private var previous = emptyList<Entry>()
  private val exits = mutableIntStateOf(0)

  fun merge(current: List<Entry>): List<Entry> {
    // Read so a finished exit recomposes the row without that item
    exits.intValue
    val currentKeys = current.mapTo(HashSet()) { it.key }
    val emitted = HashSet<Any>()
    val result = ArrayList<Entry>(current.size)
    var next = 0
    previous.fastForEach { old ->
      when {
        old.key in emitted -> Unit
        old.key in currentKeys ->
          while (next < current.size) {
            val entry = current[next++]
            if (emitted.add(entry.key)) result += entry
            if (entry.key == old.key) break
          }
        else -> {
          emitted += old.key
          result += Entry(old.key, old.weight, isRemoved = true, old.content)
        }
      }
    }
    for (i in next until current.size) {
      val entry = current[i]
      if (emitted.add(entry.key)) result += entry
    }
    return result
  }

  fun onComposed(entries: List<Entry>) {
    previous = entries
    isFirstComposition = false
  }

  fun onExited(key: Any) {
    previous = previous.filterNot { it.key == key && it.isRemoved }
    exits.intValue++
  }
}

@Preview
@Composable
private fun PreviewAnimatedFlowRow(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    val chips = remember { mutableStateListOf("Alpha", "Bravo", "Charlie", "Delta", "Echo") }
    AnimatedFlowRow(modifier = Modifier.fillMaxWidth()) {
      chips.fastForEach { chip ->
        item(key = chip) { Chip(text = chip, onClick = { chips.remove(chip) }) }
      }
      item(key = "count") { Text(text = "Count: ${chips.size}", color = colors.pageText) }
    }
  }
