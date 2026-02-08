package aktual.settings.ui

import aktual.core.icons.Info
import aktual.core.icons.MaterialIcons
import aktual.core.ui.AktualTypography
import aktual.core.ui.CardShape
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.aktualHaze
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
internal fun BasicPreferenceItem(
  title: String,
  subtitle: String?,
  icon: ImageVector?,
  clickability: Clickability,
  modifier: Modifier = Modifier,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  rightContent: (@Composable () -> Unit)? = null,
  bottomContent: (@Composable () -> Unit)? = null,
) {
  val clickableModifier = when (clickability) {
    NotClickable -> Modifier

    is Clickable -> Modifier.clickable(
      interactionSource = interactionSource,
      indication = ripple(),
      enabled = clickability.enabled,
      onClick = clickability.onClick,
    )
  }

  Row(
    modifier = modifier
      .clip(CardShape)
      .aktualHaze()
      .padding(5.dp)
      then clickableModifier,
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    if (icon != null) {
      Icon(
        modifier = Modifier
          .size(50.dp)
          .padding(10.dp),
        imageVector = icon,
        contentDescription = title,
      )
    }

    Column(
      modifier = Modifier.weight(1f),
    ) {
      Column(
        modifier = Modifier
          .wrapContentHeight()
          .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Text(
          text = title,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Start,
          style = AktualTypography.bodyLarge,
        )

        if (subtitle != null) {
          Text(
            text = subtitle,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Start,
            style = AktualTypography.bodyMedium,
          )
        }
      }

      if (bottomContent != null) {
        bottomContent()
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
  @PreviewParameter(BasicPreferenceItemProvider::class) params: ThemedParams<BasicPreferenceItemParams>,
) = PreviewWithColorScheme(params.type) {
  BasicPreferenceItem(
    title = params.data.title,
    subtitle = params.data.subtitle,
    icon = params.data.icon,
    clickability = Clickable { },
  )
}

private data class BasicPreferenceItemParams(
  val title: String,
  val subtitle: String?,
  val icon: ImageVector?,
)

private class BasicPreferenceItemProvider : ThemedParameterProvider<BasicPreferenceItemParams>(
  BasicPreferenceItemParams(
    title = "Change the doodad",
    subtitle = "When you change this setting, the doodad will update. This might also affect the thingybob.",
    icon = MaterialIcons.Info,
  ),
  BasicPreferenceItemParams(
    title = "This one has no subtitle and no icon",
    subtitle = null,
    icon = null,
  ),
)
