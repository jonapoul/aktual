package aktual.core.ui

import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.OverscrollFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch

/**
 * Wraps the platform overscroll so the stretch actually relaxes after a fling. The stretch only
 * animates while it's drawn, and the redraw it requests from within its own draw gets lost, so it
 * stays stretched and every later swipe has to unwind it before the list scrolls. Poking it once a
 * frame from outside draw keeps the release animation going.
 */
@Composable
@ReadOnlyComposable
internal fun releasingOverscrollFactory(): OverscrollFactory? {
  val default = LocalOverscrollFactory.current ?: return null
  return ReleasingOverscrollFactory(default)
}

private data class ReleasingOverscrollFactory(private val delegate: OverscrollFactory) :
  OverscrollFactory {
  override fun createOverscrollEffect(): OverscrollEffect =
    ReleasingOverscrollEffect(delegate.createOverscrollEffect())
}

private class ReleasingOverscrollEffect(private val delegate: OverscrollEffect) :
  OverscrollEffect by delegate {
  private var releaseJob: Job? = null

  override fun applyToScroll(
    delta: Offset,
    source: NestedScrollSource,
    performScroll: (Offset) -> Offset,
  ): Offset {
    releaseJob?.cancel()
    return delegate.applyToScroll(delta, source, performScroll)
  }

  override suspend fun applyToFling(
    velocity: Velocity,
    performFling: suspend (Velocity) -> Velocity,
  ) {
    releaseJob?.cancel()
    delegate.applyToFling(velocity, performFling)
    if (!delegate.isInProgress) return

    // Separate job so the fling isn't held open while the stretch relaxes
    releaseJob =
      CoroutineScope(currentCoroutineContext() + Job()).launch {
        var frames = 0
        while (delegate.isInProgress && frames++ < MAX_RELEASE_FRAMES) {
          withFrameNanos {}
          // A zero fling invalidates the effect while it's still animating
          delegate.applyToFling(Zero) { Zero }
        }
      }
  }

  private companion object {
    const val MAX_RELEASE_FRAMES = 120
  }
}
