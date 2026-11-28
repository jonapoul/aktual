package aktual.core.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/** [Icon] that fades and scales between icons when [imageVector] changes. */
@Composable
fun AnimatedIcon(
  imageVector: ImageVector,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  tint: Color = LocalContentColor.current,
) {
  AnimatedContent(
    modifier = modifier,
    targetState = IconState(imageVector, contentDescription),
    contentKey = IconState::imageVector,
    transitionSpec = {
      fadeIn() + scaleIn(initialScale = HIDDEN_SCALE) togetherWith
        fadeOut() + scaleOut(targetScale = HIDDEN_SCALE)
    },
    label = "AnimatedIcon",
  ) { state ->
    Icon(
      imageVector = state.imageVector,
      contentDescription = state.contentDescription,
      tint = tint,
    )
  }
}

private data class IconState(val imageVector: ImageVector, val contentDescription: String?)

private const val HIDDEN_SCALE = 0.6f
