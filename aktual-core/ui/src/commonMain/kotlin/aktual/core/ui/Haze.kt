package aktual.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint

@Stable
@Composable
@ReadOnlyComposable
fun defaultHazeStyle(
  theme: Theme,
  containerColor: Color = theme.pageBackground,
  degree: Float = 0.35f,
) =
  HazeStyle(
    blurRadius = 30.dp,
    backgroundColor = containerColor,
    tint =
      HazeTint(
        containerColor.copy(
          alpha = if (containerColor.luminance() >= 0.5) (1f - degree) else degree
        )
      ),
  )
