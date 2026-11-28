package aktual.core.ui

import aktual.core.icons.AktualIcons
import aktual.core.icons.CheveronDown
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer

// Chevron that points down when expanded and turns to point right when collapsed
@Composable
fun ExpandChevron(
  isExpanded: Boolean,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  tint: Color = LocalContentColor.current,
) {
  val rotation by animateExpandRotation(isExpanded)
  Icon(
    modifier = modifier.graphicsLayer { rotationZ = rotation },
    imageVector = AktualIcons.CheveronDown,
    contentDescription = contentDescription,
    tint = tint,
  )
}

@Composable
fun animateExpandRotation(isExpanded: Boolean) =
  animateFloatAsState(if (isExpanded) 0f else COLLAPSED_ROTATION, label = "ExpandRotation")

private const val COLLAPSED_ROTATION = -90f
