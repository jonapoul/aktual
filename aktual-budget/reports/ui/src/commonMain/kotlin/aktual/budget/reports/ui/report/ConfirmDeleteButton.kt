package aktual.budget.reports.ui.report

import aktual.core.icons.material.Delete
import aktual.core.icons.material.MaterialIcons
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BareIconButton
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned

// Delete buttons that take two taps. The first tilts the icon and tints it red, and the second
// deletes. A tap anywhere else inside disarmDeletesOnTap() puts it back
@Stable
internal class ConfirmDeleteState {
  var armed by mutableStateOf<DeleteTarget?>(null)
  var container: LayoutCoordinates? = null

  fun onDown(position: Offset) {
    val target = armed?.coordinates
    val container = container
    val isOnTarget =
      target != null &&
        container != null &&
        target.isAttached &&
        container.isAttached &&
        container.localBoundingBoxOf(target).contains(position)
    if (!isOnTarget) armed = null
  }
}

internal class DeleteTarget {
  var coordinates: LayoutCoordinates? = null
}

internal val LocalConfirmDeleteState = staticCompositionLocalOf { ConfirmDeleteState() }

@Composable internal fun rememberConfirmDeleteState() = remember { ConfirmDeleteState() }

internal fun Modifier.disarmDeletesOnTap(state: ConfirmDeleteState): Modifier =
  onGloballyPositioned {
    state.container = it
  }
  .pointerInput(state) {
    awaitEachGesture {
      val down = awaitFirstDown(requireUnconsumed = false, pass = Initial)
      state.onDown(down.position)
    }
  }

@Composable
internal fun ConfirmDeleteButton(
  contentDescription: String,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val state = LocalConfirmDeleteState.current
  val target = remember { DeleteTarget() }
  val isArmed = state.armed === target

  DisposableEffect(state, target) { onDispose { if (state.armed === target) state.armed = null } }

  BareIconButton(
    modifier = modifier.onGloballyPositioned { target.coordinates = it },
    imageVector = MaterialIcons.Delete,
    contentDescription = contentDescription,
    onClick = {
      if (isArmed) {
        state.armed = null
        onDelete()
      } else {
        state.armed = target
      }
    },
  ) {
    val rotation by animateFloatAsState(if (isArmed) ARMED_ROTATION else 0f)
    val tint by animateColorAsState(if (isArmed) colors.errorText else LocalContentColor.current)
    Icon(
      modifier = Modifier.rotate(rotation),
      imageVector = MaterialIcons.Delete,
      contentDescription = contentDescription,
      tint = tint,
    )
  }
}

private const val ARMED_ROTATION = 90f
