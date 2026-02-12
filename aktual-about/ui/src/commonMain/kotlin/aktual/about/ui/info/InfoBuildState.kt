package aktual.about.ui.info

import aktual.about.vm.BuildState
import aktual.core.icons.Apps
import aktual.core.icons.CalendarToday
import aktual.core.icons.Cloud
import aktual.core.icons.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.model.AktualVersions
import aktual.core.model.ColorSchemeType
import aktual.core.ui.CardShape
import aktual.core.ui.ColorSchemeParameters
import aktual.core.ui.LocalTheme
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.Theme
import aktual.core.ui.aktualHaze
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
internal fun InfoBuildState(
  buildState: BuildState,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) =
  Column(modifier) {
    BuildStateItem(
      modifier = Modifier.padding(ItemMargin).clip(CardShape).aktualHaze(),
      icon = MaterialIcons.Apps,
      title = Strings.infoAppVersion,
      subtitle = buildState.versions.app,
      theme = theme,
    )

    BuildStateItem(
      modifier =
        Modifier.testTag(Tags.ServerVersionText).padding(ItemMargin).clip(CardShape).aktualHaze(),
      icon = MaterialIcons.Cloud,
      title = Strings.infoServerVersion,
      subtitle = buildState.versions.server ?: Strings.infoServerVersionUnknown,
      theme = theme,
    )

    BuildStateItem(
      modifier = Modifier.padding(ItemMargin).clip(CardShape).aktualHaze(),
      icon = MaterialIcons.CalendarToday,
      title = Strings.infoDate,
      subtitle = buildState.buildDate,
      theme = theme,
    )
  }

private val ItemMargin = PaddingValues(horizontal = 6.dp, vertical = 3.dp)

internal val PreviewBuildState =
  BuildState(
    versions = AktualVersions(app = "1.2.3", server = "2.3.4"),
    buildDate = "12:34 GMT, 1st Feb 2024",
    year = 2024,
  )

@Preview
@Composable
private fun PreviewBuildState(
  @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType
) =
  PreviewWithColorScheme(type) {
    InfoBuildState(modifier = Modifier.fillMaxWidth(), buildState = PreviewBuildState)
  }
