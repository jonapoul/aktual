package aktual.about.ui.info

import aktual.about.vm.BuildState
import aktual.core.icons.Apps
import aktual.core.icons.CalendarToday
import aktual.core.icons.Cloud
import aktual.core.icons.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.model.AktualVersions
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.AktualTypography
import aktual.core.ui.CardShape
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.ThemeParameters
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
  Column(modifier = modifier.background(theme.pillBackgroundLight, CardShape)) {
    BuildStateItem(
      modifier = Modifier.padding(ItemMargin).clip(CardShape),
      icon = MaterialIcons.Apps,
      title = Strings.infoAppVersion,
      subtitle = buildState.versions.app,
      theme = theme,
    )

    BuildStateItem(
      modifier = Modifier.testTag(Tags.ServerVersionText).padding(ItemMargin).clip(CardShape),
      icon = MaterialIcons.Cloud,
      title = Strings.infoServerVersion,
      subtitle = buildState.versions.server ?: Strings.infoServerVersionUnknown,
      theme = theme,
    )

    BuildStateItem(
      modifier = Modifier.padding(ItemMargin).clip(CardShape),
      icon = MaterialIcons.CalendarToday,
      title = Strings.infoDate,
      subtitle = buildState.buildDate,
      theme = theme,
    )
  }

private val ItemMargin = PaddingValues(horizontal = 6.dp, vertical = 3.dp)

@Composable
private fun BuildStateItem(
  icon: ImageVector,
  title: String,
  subtitle: String,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
  onClick: (() -> Unit)? = null,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .height(ItemHeight)
        .background(Color.Transparent, CardShape)
        .clickable(enabled = onClick != null, onClick = { onClick?.invoke() })
        .padding(horizontal = ItemPadding),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      modifier = Modifier.padding(ItemPadding),
      imageVector = icon,
      contentDescription = title,
      tint = theme.pillText,
    )

    Column(modifier = Modifier.weight(1f).padding(ItemPadding)) {
      Text(
        modifier = Modifier.testTag(Tags.BuildStateItemTitle),
        text = title,
        style = AktualTypography.bodyLarge,
        color = theme.pillText,
      )
      Text(
        modifier = Modifier.testTag(Tags.BuildStateItemValue),
        text = subtitle,
        style = AktualTypography.labelMedium,
        color = theme.pillTextSubdued,
      )
    }
  }
}

private val ItemPadding = 8.dp
private val ItemHeight = 50.dp

internal val PreviewBuildState =
  BuildState(
    versions = AktualVersions(app = "1.2.3", server = "2.3.4"),
    buildDate = "12:34 GMT, 1st Feb 2024",
    year = 2024,
  )

@Preview
@Composable
private fun PreviewBuildState(@PreviewParameter(ThemeParameters::class) theme: Theme) =
  PreviewWithColorScheme(theme) {
    InfoBuildState(modifier = Modifier.fillMaxWidth(), buildState = PreviewBuildState)
  }
