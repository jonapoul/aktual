package aktual.about.ui.info

import aktual.about.vm.BuildState
import aktual.core.model.AktualVersions
import aktual.core.model.ColorSchemeType
import aktual.core.ui.ColorSchemeParameters
import aktual.core.ui.LocalTheme
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.Theme
import aktual.core.ui.defaultHazeStyle
import aktual.l10n.Strings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect

@Composable
internal fun InfoBuildState(
  buildState: BuildState,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
  hazeState: HazeState = remember { HazeState() },
  hazeStyle: HazeStyle = defaultHazeStyle(theme),
) = Column(modifier) {
  BuildStateItem(
    modifier = Modifier
      .padding(ItemMargin)
      .hazeEffect(hazeState, hazeStyle),
    icon = Icons.Filled.Apps,
    title = Strings.infoAppVersion,
    subtitle = buildState.versions.app,
  )

  BuildStateItem(
    modifier = Modifier
      .testTag(Tags.ServerVersionText)
      .padding(ItemMargin)
      .hazeEffect(hazeState, hazeStyle),
    icon = Icons.Filled.Cloud,
    title = Strings.infoServerVersion,
    subtitle = buildState.versions.server ?: Strings.infoServerVersionUnknown,
  )

  BuildStateItem(
    modifier = Modifier
      .padding(ItemMargin)
      .hazeEffect(hazeState, hazeStyle),
    icon = Icons.Filled.CalendarToday,
    title = Strings.infoDate,
    subtitle = buildState.buildDate,
  )
}

private val ItemMargin = PaddingValues(horizontal = 6.dp, vertical = 3.dp)

internal val PreviewBuildState = BuildState(
  versions = AktualVersions(app = "1.2.3", server = "2.3.4"),
  buildDate = "12:34 GMT, 1st Feb 2024",
  year = 2024,
)

@Preview
@Composable
private fun PreviewBuildState(
  @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType,
) = PreviewWithColorScheme(type) {
  InfoBuildState(
    modifier = Modifier.fillMaxWidth(),
    buildState = PreviewBuildState,
  )
}
