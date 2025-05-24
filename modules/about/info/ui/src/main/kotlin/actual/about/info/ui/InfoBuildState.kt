package actual.about.info.ui

import actual.about.info.res.InfoStrings
import actual.about.info.vm.BuildState
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewScreen
import actual.core.ui.ScreenPreview
import actual.core.ui.Theme
import actual.core.ui.defaultHazeStyle
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect

@Composable
internal fun InfoBuildState(
  buildState: BuildState,
  onAction: (InfoAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
  hazeState: HazeState = remember { HazeState() },
  hazeStyle: HazeStyle = defaultHazeStyle(theme),
) {
  Column(modifier) {
    BuildStateItem(
      modifier = Modifier
        .padding(ItemMargin)
        .hazeEffect(hazeState, hazeStyle),
      icon = Icons.Filled.Apps,
      title = InfoStrings.appVersion,
      subtitle = buildState.versions.app,
    )

    BuildStateItem(
      modifier = Modifier
        .testTag(Tags.ServerVersionText)
        .padding(ItemMargin)
        .hazeEffect(hazeState, hazeStyle),
      icon = Icons.Filled.Cloud,
      title = InfoStrings.serverVersion,
      subtitle = buildState.versions.server ?: InfoStrings.serverVersionUnknown,
    )

    BuildStateItem(
      modifier = Modifier
        .padding(ItemMargin)
        .hazeEffect(hazeState, hazeStyle),
      icon = Icons.Filled.CalendarToday,
      title = InfoStrings.date,
      subtitle = buildState.buildDate,
    )

    BuildStateItem(
      modifier = Modifier
        .testTag(Tags.SourceCodeButton)
        .padding(ItemMargin)
        .hazeEffect(hazeState, hazeStyle),
      icon = Icons.Filled.Code,
      title = InfoStrings.repo,
      subtitle = buildState.sourceCodeRepo,
      onClick = { onAction(InfoAction.OpenSourceCode) },
    )
  }
}

private val ItemMargin = PaddingValues(horizontal = 6.dp, vertical = 3.dp)

@ScreenPreview
@Composable
private fun PreviewBuildState() = PreviewScreen {
  InfoBuildState(
    modifier = Modifier.fillMaxWidth(),
    buildState = PreviewBuildState,
    onAction = {},
  )
}
