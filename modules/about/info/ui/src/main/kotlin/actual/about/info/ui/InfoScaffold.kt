package actual.about.info.ui

import actual.about.info.vm.BuildState
import actual.core.model.ColorSchemeType
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewScreen
import actual.core.ui.PreviewWithColorScheme
import actual.core.ui.ScreenPreview
import actual.core.ui.Theme
import actual.core.ui.WavyBackground
import alakazam.android.ui.compose.VerticalSpacer
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import my.nanihadesuka.compose.ColumnScrollbar

@Composable
internal fun InfoScaffold(
  buildState: BuildState,
  onAction: (InfoAction) -> Unit,
) {
  val theme = LocalTheme.current
  Scaffold(
    topBar = { InfoTopBar(theme, onAction) },
  ) { innerPadding ->
    Box {
      val hazeState = remember { HazeState() }

      WavyBackground(
        modifier = Modifier
          .hazeSource(hazeState)
          .background(Color.Red),
      )

      InfoScreenContent(
        modifier = Modifier
          .padding(innerPadding)
          .scrollable(
            state = rememberScrollableState { delta -> delta },
            orientation = Orientation.Vertical,
          ),
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
) {
  val scrollState = rememberScrollState()
  ColumnScrollbar(
    modifier = modifier.fillMaxSize(),
    state = scrollState,
  ) {
    Column(
      modifier = Modifier
        .testTag(Tags.InfoScreenContent)
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(15.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      InfoHeader(
        modifier = Modifier
          .wrapContentWidth()
          .padding(vertical = 5.dp),
        year = buildState.year,
        theme = theme,
      )

      InfoBuildState(
        modifier = Modifier.fillMaxWidth(),
        buildState = buildState,
        hazeState = hazeState,
        theme = theme,
        onAction = onAction,
      )

      VerticalSpacer(weight = 1f)

      InfoButtons(
        modifier = Modifier
          .wrapContentHeight()
          .fillMaxWidth(),
        onAction = onAction,
      )
    }
  }
}

@ScreenPreview
@Composable
private fun PreviewInfo() = PreviewScreen {
  InfoScaffold(
    buildState = PreviewBuildState,
    onAction = {},
  )
}
