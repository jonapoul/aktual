package aktual.budget.rules.ui.edit

import aktual.core.icons.Add
import aktual.core.icons.AktualIcons
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.Dimens
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * One row of the timeline. A step starts with a row holding its [node], and the rail runs down the
 * gutter of every row in a step with another step after it.
 */
@Composable
internal fun TimelineRow(
  hasRail: Boolean,
  modifier: Modifier = Modifier,
  node: (@Composable () -> Unit)? = null,
  isEndOfStep: Boolean = false,
  content: @Composable ColumnScope.() -> Unit,
) {
  val railColor = colors.tableBorderSeparator
  Row(
    modifier =
      modifier.fillMaxWidth().drawBehind {
        if (hasRail) {
          val x = NodeSize.toPx() / 2
          drawLine(
            color = railColor,
            start = Offset(x, if (node == null) 0f else NodeSize.toPx()),
            end = Offset(x, size.height),
            strokeWidth = RailWidth.toPx(),
          )
        }
      },
    horizontalArrangement = Arrangement.spacedBy(Dimens.VeryLarge),
  ) {
    Box(modifier = Modifier.width(NodeSize)) {
      if (node != null) {
        Box(modifier = Modifier.size(NodeSize), contentAlignment = Center) { node() }
      }
    }
    Column(
      modifier =
        Modifier.weight(1f).padding(bottom = if (isEndOfStep) NodeSpacing else Dimens.Large),
      verticalArrangement = Arrangement.spacedBy(Dimens.Large),
      content = content,
    )
  }
}

@Composable
internal fun DotNode(modifier: Modifier = Modifier) =
  Box(modifier = modifier.size(DotSize).background(colors.pageTextSubdued, CircleShape))

@Composable
internal fun IconNode(
  icon: ImageVector,
  background: Color,
  foreground: Color,
  modifier: Modifier = Modifier,
) =
  Box(
    modifier = modifier.size(NodeSize).background(background, CircleShape),
    contentAlignment = Center,
  ) {
    Icon(
      modifier = Modifier.size(NodeIconSize),
      imageVector = icon,
      contentDescription = null,
      tint = foreground,
    )
  }

// Lines up with the node so the heading sits level with it
@Composable
internal fun NodeHeading(
  text: String,
  modifier: Modifier = Modifier,
  trailing: @Composable RowScope.() -> Unit = {},
) =
  Row(
    modifier = modifier.fillMaxWidth().heightIn(min = NodeSize),
    horizontalArrangement = Arrangement.spacedBy(Dimens.VeryLarge),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(text = text, style = typography.titleMedium, color = colors.pageText)
    trailing()
  }

@Composable
internal fun AddCard(
  text: String,
  onClick: () -> Unit,
  isEnabled: Boolean,
  modifier: Modifier = Modifier,
) {
  val border = colors.tableBorderSeparator
  val content = if (isEnabled) colors.pageTextSubdued else colors.tableTextInactive
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(CardShape)
        .drawBehind {
          val width = 1.dp.toPx()
          val dash = 4.dp.toPx()
          drawRoundRect(
            color = border,
            topLeft = Offset(width / 2, width / 2),
            size = size.copy(width = size.width - width, height = size.height - width),
            cornerRadius = CornerRadius(4.dp.toPx()),
            style =
              Stroke(
                width = width,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash)),
              ),
          )
        }
        .clickable(enabled = isEnabled, onClick = onClick)
        .padding(Dimens.VeryLarge),
    horizontalArrangement = Arrangement.spacedBy(Dimens.Large),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      modifier = Modifier.size(16.dp),
      imageVector = AktualIcons.Add,
      contentDescription = null,
      tint = content,
    )
    Text(text = text, style = typography.bodyMedium, color = content)
  }
}

internal val NodeSize = 28.dp
private val NodeIconSize = 16.dp
private val DotSize = 10.dp
private val RailWidth = 2.dp
private val NodeSpacing = 24.dp
