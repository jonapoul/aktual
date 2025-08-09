package actual.about.ui.info

import actual.about.vm.BuildState
import actual.core.ui.CardShape
import actual.core.ui.LocalTheme
import actual.core.ui.Theme
import actual.core.ui.WavyBackground
import actual.core.ui.defaultHazeStyle
import actual.core.ui.verticalScrollWithBar
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource

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
        modifier = Modifier.hazeSource(hazeState),
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
  Column(
    modifier = modifier.fillMaxSize(),
  ) {
    val scrollState = rememberScrollState()
    val hazeStyle = defaultHazeStyle(theme)
    Column(
      modifier = Modifier
        .testTag(Tags.InfoScreenContent)
        .weight(1f)
        .fillMaxWidth()
        .verticalScrollWithBar(scrollState)
        .padding(horizontal = 15.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 6.dp, vertical = 12.dp)
          .background(Color.Transparent, CardShape)
          .hazeEffect(hazeState, hazeStyle),
      ) {
        InfoHeader(
          modifier = Modifier.fillMaxWidth(),
          year = buildState.year,
          theme = theme,
        )
      }

      InfoBuildState(
        modifier = Modifier.fillMaxWidth(),
        buildState = buildState,
        hazeState = hazeState,
        hazeStyle = hazeStyle,
        theme = theme,
      )
    }

    InfoButtons(
      modifier = Modifier
        .wrapContentHeight()
        .fillMaxWidth()
        .padding(horizontal = 15.dp),
      onAction = onAction,
    )
  }
}
