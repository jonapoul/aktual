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

// Chevron that animates between its collapsed and expanded directions, given by [style]
@Composable
fun ExpandChevron(
  isExpanded: Boolean,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  style: ExpandChevronStyle = RightToDown,
  tint: Color = LocalContentColor.current,
) {
  val rotation by animateExpandRotation(isExpanded, style)
  Icon(
    modifier = modifier.graphicsLayer { rotationZ = rotation },
    imageVector = AktualIcons.CheveronDown,
    contentDescription = contentDescription,
    tint = tint,
  )
}

enum class ExpandChevronStyle(val collapsedRotation: Float, val expandedRotation: Float) {
  // Points right when collapsed, down when expanded
  RightToDown(collapsedRotation = -90f, expandedRotation = 0f),

  // Points down when collapsed, up when expanded
  DownToUp(collapsedRotation = 0f, expandedRotation = 180f),
}

@Composable
fun animateExpandRotation(isExpanded: Boolean, style: ExpandChevronStyle = RightToDown) =
  animateFloatAsState(
    targetValue = if (isExpanded) style.expandedRotation else style.collapsedRotation,
    label = "ExpandRotation",
  )
