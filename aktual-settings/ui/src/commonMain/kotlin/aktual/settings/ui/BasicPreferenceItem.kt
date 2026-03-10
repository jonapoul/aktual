package aktual.settings.ui

import aktual.core.icons.material.Info
import aktual.core.icons.material.MaterialIcons
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.AktualTypography
import aktual.core.ui.CardShape
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import alakazam.kotlin.ifNotNull
import alakazam.kotlin.ifTrue
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun BasicPreferenceItem(
  title: String,
  subtitle: String?,
  icon: ImageVector?,
  onClick: (() -> Unit)?,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  includeBackground: Boolean = true,
  headerStyle: TextStyle = AktualTypography.bodyLarge,
  theme: Theme = LocalTheme.current,
  rightContent: (@Composable RowScope.() -> Unit)? = null,
  bottomContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
  val contentColor = if (enabled) theme.pageText else theme.pageTextSubdued

  Row(
    modifier =
      modifier
        .clip(CardShape)
        .ifTrue(includeBackground) { background(theme.pillBackgroundLight, CardShape) }
        .ifTrue(includeBackground) { border(Dp.Hairline, theme.pillBorderDark, CardShape) }
        .ifNotNull(onClick) { clickable(enabled, onClick = it) }
        .ifTrue(includeBackground) { padding(5.dp) },
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    if (icon != null) {
      Icon(
        modifier = Modifier.size(50.dp).padding(10.dp),
        imageVector = icon,
        contentDescription = title,
        tint = contentColor,
      )
    }

    Column(modifier = Modifier.weight(1f)) {
      Column(
        modifier = Modifier.wrapContentHeight().padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Text(
          text = title,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Start,
          style = headerStyle,
          color = contentColor,
        )

        if (subtitle != null) {
          Text(
            text = subtitle,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Start,
            style = AktualTypography.bodyMedium,
            color = if (enabled) theme.pageTextLight else theme.pageTextSubdued,
          )
        }
      }

      if (bottomContent != null) {
        Column(modifier = Modifier.padding(10.dp)) { bottomContent() }
      }
    }

    if (rightContent != null) {
      rightContent()
    }
  }
}

@Preview
@Composable
private fun PreviewBasicPreferenceItem(
  @PreviewParameter(BasicPreferenceItemProvider::class)
  params: ThemedParams<BasicPreferenceItemParams>
) =
  PreviewWithColorScheme(params.theme) {
    BasicPreferenceItem(
      title = params.data.title,
      subtitle = params.data.subtitle,
      icon = params.data.icon,
      onClick = {},
      enabled = params.data.enabled,
    )
  }

private data class BasicPreferenceItemParams(
  val title: String,
  val subtitle: String?,
  val icon: ImageVector?,
  val enabled: Boolean = true,
)

private class BasicPreferenceItemProvider :
  ThemedParameterProvider<BasicPreferenceItemParams>(
    BasicPreferenceItemParams(
      title = "Change the doodad",
      subtitle =
        "When you change this setting, the doodad will update. This might also affect the thingybob.",
      icon = MaterialIcons.Info,
    ),
    BasicPreferenceItemParams(
      title = "This one has no subtitle and no icon",
      subtitle = null,
      icon = null,
    ),
    BasicPreferenceItemParams(
      title = "Disabled preference",
      subtitle = "This item is disabled and should appear subdued",
      icon = MaterialIcons.Info,
      enabled = false,
    ),
  )
