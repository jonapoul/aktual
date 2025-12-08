package aktual.core.ui

import aktual.core.model.ColorSchemeType
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource

@Composable
fun HazedBox(
  state: HazeState,
  modifier: Modifier = Modifier,
  contentAlignment: Alignment = Alignment.TopStart,
  padding: PaddingValues = PaddingValues(30.dp),
  shape: Shape = RounderCardShape,
  style: HazeStyle = defaultHazeStyle(LocalTheme.current),
  content: @Composable BoxScope.() -> Unit,
) = Box(
  modifier
    .clip(shape)
    .hazeEffect(state, style)
    .padding(padding),
  contentAlignment = contentAlignment,
  content = content,
)

@Preview
@Composable
private fun PreviewLoading(
  @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType,
) = PreviewBasic(type) { theme ->
  CircularProgressIndicator(
    modifier = Modifier
      .align(Alignment.Center)
      .size(50.dp),
    color = theme.buttonPrimaryBackground,
    trackColor = theme.dialogProgressWheelTrack,
  )
}

@Preview
@Composable
private fun PreviewText(
  @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType,
) = PreviewBasic(type) {
  Text("Hello world")
}

@Composable
private fun PreviewBasic(
  type: ColorSchemeType,
  size: DpSize = DpSize(200.dp, 200.dp),
  content: @Composable BoxScope.(Theme) -> Unit,
) = PreviewWithColorScheme(type) {
  val theme = LocalTheme.current
  val hazeState = remember { HazeState() }
  Box(
    modifier = Modifier.size(size),
  ) {
    WavyBackground(
      modifier = Modifier
        .fillMaxSize()
        .background(theme.dialogBackground)
        .hazeSource(hazeState),
    )

    HazedBox(
      modifier = Modifier.align(Alignment.Center),
      state = hazeState,
    ) {
      content(theme)
    }
  }
}
