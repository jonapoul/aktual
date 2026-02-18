package aktual.core.ui

import aktual.core.model.ColorSchemeType
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

// packages/component-library/src/icons/AnimatedLoading.tsx
// packages/component-library/src/icons/Loading.tsx
@Composable
fun AnimatedLoading(modifier: Modifier = Modifier, theme: Theme = LocalTheme.current) {
  val infiniteTransition = rememberInfiniteTransition(label = "spin")

  val rotation by
    infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 360f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(DURATION_MS, easing = LinearEasing),
          repeatMode = RepeatMode.Restart,
        ),
      label = "rotation",
    )

  Image(
    imageVector = remember(theme.pageText) { loadingIcon(theme.pageText) },
    contentDescription = null,
    modifier = modifier.rotate(rotation),
  )
}

private const val DURATION_MS = 1600

@Stable
@Suppress("MagicNumber")
private fun loadingIcon(color: Color): ImageVector =
  ImageVector.Builder(
      name = "LoadingIcon",
      defaultWidth = 38.dp,
      defaultHeight = 38.dp,
      viewportWidth = 38f,
      viewportHeight = 38f,
    )
    .apply {
      group(translationX = 1f, translationY = 2f) {
        path(
          stroke =
            Brush.linearGradient(
              0.0f to Color.Transparent,
              0.631f to color.copy(alpha = 0.631f),
              1.0f to color,
              start = Offset(3.05f, 0f),
              end = Offset(24.95f, 9.06f),
            ),
          strokeLineWidth = 2f,
        ) {
          moveTo(36f, 18f)
          curveTo(36f, 8.06f, 27.94f, 0f, 18f, 0f)
        }

        path(fill = SolidColor(color)) {
          // Move to the top of the circle
          moveTo(36f, 17f)
          // Draw the first half-circle to the bottom
          arcTo(
            horizontalEllipseRadius = 1f,
            verticalEllipseRadius = 1f,
            theta = 0f,
            isMoreThanHalf = false,
            isPositiveArc = true,
            x1 = 36f,
            y1 = 19f,
          )
          // Draw the second half-circle back to the top
          arcTo(
            horizontalEllipseRadius = 1f,
            verticalEllipseRadius = 1f,
            theta = 0f,
            isMoreThanHalf = false,
            isPositiveArc = true,
            x1 = 36f,
            y1 = 17f,
          )
          close()
        }
      }
    }
    .build()

@Preview
@Composable
private fun PreviewCheckUpdatesContent(
  @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType
) =
  PreviewWithColorScheme(type) {
    Box(modifier = Modifier.size(50.dp), contentAlignment = Alignment.Center) { AnimatedLoading() }
  }

@Preview
@Composable
private fun PreviewScaled() =
  PreviewWithColorScheme(ColorSchemeType.Light) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      AnimatedLoading(modifier = Modifier.size(20.dp))
      AnimatedLoading(modifier = Modifier.size(50.dp))
      AnimatedLoading(modifier = Modifier.size(150.dp))
    }
  }
