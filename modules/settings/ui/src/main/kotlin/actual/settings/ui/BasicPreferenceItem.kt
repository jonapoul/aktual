package actual.settings.ui

import actual.core.ui.CardShape
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewColumn
import actual.core.ui.Theme
import actual.core.ui.defaultHazeStyle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect

@Composable
internal fun BasicPreferenceItem(
  title: String,
  subtitle: String?,
  icon: ImageVector?,
  clickability: Clickability,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  hazeState: HazeState = remember { HazeState() },
  hazeStyle: HazeStyle = defaultHazeStyle(theme),
  content: (@Composable () -> Unit)? = null,
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
      .hazeEffect(hazeState, hazeStyle)
      .padding(5.dp)
      .background(Color.Transparent, CardShape)
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
      ) {
        Text(
          text = title,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Start,
          style = MaterialTheme.typography.bodyLarge,
        )

        if (subtitle != null) {
          Text(
            text = subtitle,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.bodyMedium,
          )
        }
      }

      if (content != null) {
        content()
      }
    }
  }
}

@Preview
@Composable
private fun WithIcon() = PreviewColumn {
  BasicPreferenceItem(
    title = "Change the doodad",
    subtitle = "When you change this setting, the doodad will update. This might also affect the thingybob.",
    icon = Icons.Filled.Info,
    clickability = Clickable { },
  )
}

@Preview
@Composable
private fun WithoutIcon() = PreviewColumn {
  BasicPreferenceItem(
    title = "Change the doodad",
    subtitle = "When you change this setting, the doodad will update. This might also affect the thingybob.",
    icon = null,
    clickability = Clickable { },
  )
}
