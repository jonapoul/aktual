package aktual.about.ui.info

import aktual.about.vm.BuildState
import aktual.core.model.ColorSchemeType
import aktual.core.ui.BottomNavBarSpacing
import aktual.core.ui.BottomStatusBarSpacing
import aktual.core.ui.CardShape
import aktual.core.ui.ColorSchemeParameters
import aktual.core.ui.Dimens
import aktual.core.ui.LandscapePreview
import aktual.core.ui.LocalTheme
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.RounderCardShape
import aktual.core.ui.TabletPreview
import aktual.core.ui.Theme
import aktual.core.ui.WavyBackground
import aktual.core.ui.defaultHazeStyle
import aktual.core.ui.verticalScrollWithBar
import alakazam.kotlin.compose.VerticalSpacer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource

@Composable
internal fun InfoScaffold(
  buildState: BuildState,
  onAction: (InfoAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  val theme = LocalTheme.current
  Scaffold(
    modifier = modifier,
    topBar = { InfoTopBar(theme, onAction) },
  ) { innerPadding ->
    Box {
      val hazeState = remember { HazeState() }

      WavyBackground(
        modifier = Modifier.hazeSource(hazeState),
      )

      InfoScreenContent(
        modifier = Modifier.padding(innerPadding),
        buildState = buildState,
        hazeState = hazeState,
        onAction = onAction,
        theme = theme,
      )
    }
  }
}

@Composable
private fun InfoScreenContent(
  buildState: BuildState,
  hazeState: HazeState,
  onAction: (InfoAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Column(
  modifier = modifier.verticalScrollWithBar(),
) {
  val hazeStyle = defaultHazeStyle(theme)

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(Dimens.VeryLarge)
      .clip(RounderCardShape)
      .hazeEffect(hazeState, hazeStyle),
  ) {
    InfoHeader(
      modifier = Modifier.fillMaxWidth(),
      year = buildState.year,
      theme = theme,
    )
  }

  InfoBuildState(
    modifier = Modifier
      .fillMaxWidth()
      .padding(Dimens.VeryLarge),
    buildState = buildState,
    hazeState = hazeState,
    hazeStyle = hazeStyle,
    theme = theme,
  )

  VerticalSpacer(Dimens.Huge)

  InfoButtons(
    modifier = Modifier
      .wrapContentHeight()
      .fillMaxWidth()
      .padding(Dimens.VeryLarge),
    onAction = onAction,
  )

  BottomStatusBarSpacing()
  BottomNavBarSpacing()
}

@PortraitPreview
@LandscapePreview
@TabletPreview
@Composable
private fun PreviewInfoScaffold(
  @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType,
) = PreviewWithColorScheme(type) {
  InfoScaffold(
    modifier = Modifier.fillMaxSize(),
    buildState = PreviewBuildState,
    onAction = {},
  )
}
