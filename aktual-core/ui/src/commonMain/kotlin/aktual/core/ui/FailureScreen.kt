package aktual.core.ui

import aktual.core.icons.AktualIcons
import aktual.core.icons.CloudWarning
import aktual.core.icons.Refresh
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.textSizes
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
fun FailureCard(
  title: String,
  reason: String?,
  action: FailureAction?,
  modifier: Modifier = Modifier,
  icon: ImageVector? = AktualIcons.CloudWarning,
  background: Color = colors.cardBackground,
) {
  Column(
    modifier =
      modifier
        .padding(30.dp)
        .clip(RounderCardShape)
        .background(background, RounderCardShape)
        .border(Hairline, colors.cardShadow, RounderCardShape)
        .padding(30.dp),
    horizontalAlignment = CenterHorizontally,
  ) {
    if (icon != null) {
      Icon(
        modifier = Modifier.size(100.dp),
        imageVector = icon,
        tint = colors.warningText,
        contentDescription = title,
      )

      VerticalSpacer(30.dp)
    }

    Text(
      text = title,
      color = colors.warningText,
      textAlign = Center,
      fontSize = textSizes.extraLarge,
      fontWeight = Bold,
    )

    if (reason != null) {
      VerticalSpacer(15.dp)

      Text(
        text = reason,
        color = colors.warningTextDark,
        fontSize = textSizes.large,
        textAlign = Center,
      )
    }

    if (action != null) {
      VerticalSpacer(30.dp)

      AktualButton(
        kind = Primary,
        icon = action.icon,
        text = action.text(),
        isEnabled = action.enabled,
        onClick = action.onClick,
      )
    }
  }
}

@Composable
fun FailureScreen(
  title: String,
  reason: String?,
  action: FailureAction?,
  modifier: Modifier = Modifier,
  icon: ImageVector? = AktualIcons.CloudWarning,
) {
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Center) {
    Column {
      FailureCard(
        title = title,
        reason = reason,
        icon = icon,
        action = action,
        background = colors.cardBackground,
      )

      BottomSpacing()
    }
  }
}

@Stable
data class FailureAction(
  val text: @Composable () -> String,
  val enabled: Boolean = true,
  val icon: ImageVector,
  val onClick: () -> Unit,
)

@Preview
@Composable
private fun PreviewFailureCard(
  @PreviewParameter(FailureScreenProvider::class) params: ColorParams<FailureScreenParams>,
) =
  PreviewWithColorParams(params) {
    FailureCard(
      title = title,
      reason = reason,
      action = action,
    )
  }

@PortraitPreview
@Composable
private fun PreviewFailureScreen(
  @PreviewParameter(FailureScreenProvider::class) params: ColorParams<FailureScreenParams>,
) =
  PreviewWithColorParams(params) {
    FailureScreen(
      title = title,
      reason = reason,
      action = action,
    )
  }

private data class FailureScreenParams(
  val reason: String? = null,
  val title: String = "Failed syncing the doodads",
  val action: FailureAction? =
    FailureAction(text = { "Retry" }, icon = AktualIcons.Refresh, onClick = {}),
)

private class FailureScreenProvider :
  ColorParamsProvider<FailureScreenParams>(
    FailureScreenParams(reason = "Some error"),
    FailureScreenParams(
      reason =
        "Failed to do the thing, here's a bit more text to show how it behaves when wrapping",
    ),
    FailureScreenParams(action = null),
  )
