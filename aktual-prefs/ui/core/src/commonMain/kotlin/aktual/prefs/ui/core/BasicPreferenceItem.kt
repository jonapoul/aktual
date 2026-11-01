package aktual.prefs.ui.core

import aktual.core.icons.AktualIcons
import aktual.core.icons.InformationOutline
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.ProvideSurfaceColor
import aktual.core.ui.disabledIf
import alakazam.kotlin.ifNotNull
import alakazam.kotlin.ifTrue
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
fun BasicPreferenceItem(
  title: String,
  subtitle: String?,
  icon: ImageVector?,
  onClick: (() -> Unit)?,
  modifier: Modifier = Modifier,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  enabled: Boolean = true,
  includeBackground: Boolean = true,
  headerStyle: TextStyle = typography.bodyLarge,
  topRightContent: (@Composable BoxScope.() -> Unit)? = null,
  rightContent: (@Composable RowScope.() -> Unit)? = null,
  bottomContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
  val contentColor = colors.settingsCardText.disabledIf(!enabled)
  val includeBackground = includeBackground && !LocalInPreferenceGroup.current

  Row(
    modifier =
      modifier
        .clip(CardShape)
        .ifTrue(includeBackground) { background(colors.settingsCardBackground, CardShape) }
        .ifTrue(includeBackground) { border(1.dp, colors.settingsCardBorder, CardShape) }
        .ifNotNull(onClick) {
          clickable(enabled, onClick = it, interactionSource = interactionSource)
        }
        .ifTrue(includeBackground) { padding(5.dp) },
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = CenterVertically,
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
      Box(modifier = Modifier.fillMaxWidth().padding(6.dp)) {
        Column(
          modifier = Modifier.wrapContentHeight(),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            text = title,
            fontWeight = Bold,
            textAlign = Start,
            style = headerStyle,
            color = contentColor,
          )

          if (subtitle != null) {
            Text(
              text = subtitle,
              fontWeight = Light,
              textAlign = Start,
              style = typography.labelLarge,
              color = colors.settingsCardTextSubdued.disabledIf(!enabled),
            )
          }
        }

        if (topRightContent != null) {
          Box(modifier = Modifier.align(Alignment.TopEnd), content = topRightContent)
        }
      }

      if (bottomContent != null) {
        if (includeBackground) {
          ProvideSurfaceColor(colors.settingsCardBackground) { Column(content = bottomContent) }
        } else {
          Column(content = bottomContent)
        }
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
  params: ColorParams<BasicPreferenceItemParams>,
) =
  PreviewWithColorParams(params) {
    BasicPreferenceItem(
      title = title,
      subtitle = subtitle,
      icon = icon,
      onClick = {},
      enabled = enabled,
    )
  }

private data class BasicPreferenceItemParams(
  val title: String,
  val subtitle: String?,
  val icon: ImageVector?,
  val enabled: Boolean = true,
)

private class BasicPreferenceItemProvider :
  ColorParamsProvider<BasicPreferenceItemParams>(
    BasicPreferenceItemParams(
      title = "Change the doodad",
      subtitle =
        "When you change this setting, the doodad will update. This might also affect the thingybob.",
      icon = AktualIcons.InformationOutline,
    ),
    BasicPreferenceItemParams(
      title = "This one has no subtitle and no icon",
      subtitle = null,
      icon = null,
    ),
    BasicPreferenceItemParams(
      title = "Disabled preference",
      subtitle = "This item is disabled and should appear subdued",
      icon = AktualIcons.InformationOutline,
      enabled = false,
    ),
  )
