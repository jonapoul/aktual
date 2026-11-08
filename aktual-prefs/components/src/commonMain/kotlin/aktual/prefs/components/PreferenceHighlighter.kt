package aktual.prefs.components

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

// Scrolls to and flashes the preference or group with this title. group is the title of the
// PreferenceGroup holding it, or null when it isn't inside one
@Stable
class PreferenceHighlighter
internal constructor(
  private val group: String?,
  private val title: String,
  private val onFinish: () -> Unit,
) {
  internal var requester by mutableStateOf<BringIntoViewRequester?>(null)

  internal fun matches(group: String?, title: String) = this.group == group && this.title == title

  internal fun finish() = onFinish()
}

val LocalPreferenceHighlighter = compositionLocalOf<PreferenceHighlighter?> { null }

// Null once the highlight has run, so it doesn't run again when coming back to the screen
@Composable
fun rememberPreferenceHighlighter(group: String?, title: String?): PreferenceHighlighter? {
  var isFinished by rememberSaveable { mutableStateOf(false) }
  if (title == null || isFinished) return null
  return remember(group, title) {
    PreferenceHighlighter(group, title, onFinish = { isFinished = true })
  }
}

@Composable
fun ScrollToHighlight(highlighter: PreferenceHighlighter, listState: LazyListState) {
  LaunchedEffect(highlighter) {
    withFrameNanos {}

    // Lazy items aren't composed until they're near the viewport, so keep scrolling down until
    // the target composes and registers itself
    while (highlighter.requester == null && listState.canScrollForward) {
      listState.scrollBy(listState.layoutInfo.viewportSize.height / 2f)
      withFrameNanos {}
    }

    highlighter.requester?.bringIntoView()
    delay(HighlightDuration)
    highlighter.finish()
  }
}

private val HighlightDuration = 2.seconds
