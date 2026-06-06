package aktual.core.ui

import aktual.core.icons.AktualIcons
import aktual.core.icons.CloudWarning
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Refresh
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    modifier = modifier.padding(30.dp).background(background, CardShape).padding(30.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
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
      textAlign = TextAlign.Center,
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold,
    )

    if (reason != null) {
      VerticalSpacer(15.dp)

      Text(
        text = reason,
        color = colors.warningTextDark,
        fontSize = 16.sp,
        textAlign = TextAlign.Center,
      )
    }

    if (action != null) {
      VerticalSpacer(30.dp)

      PrimaryTextButton(
        prefix = { Icon(imageVector = action.icon, contentDescription = action.text()) },
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
  background: Color = Color.Transparent,
) {
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Column {
      FailureCard(
        title = title,
        reason = reason,
        icon = icon,
        action = action,
        background = background,
      )

      BottomSpacing()
    }
  }
}

@Immutable
data class FailureAction(
  val text: @Composable () -> String,
  val enabled: Boolean = true,
  val icon: ImageVector,
  val onClick: () -> Unit,
)

@Preview
@Composable
private fun PreviewFailureCard(
  @PreviewParameter(FailureScreenProvider::class) params: ColoredParams<FailureScreenParams>
) =
  PreviewWithColoredParams(params) {
    FailureCard(
      title = title,
      reason = reason,
      action = action,
      background = colors.background(),
    )
  }

@PortraitPreview
@Composable
private fun PreviewFailureScreen(
  @PreviewParameter(FailureScreenProvider::class) params: ColoredParams<FailureScreenParams>
) =
  PreviewWithColoredParams(params) {
    FailureScreen(
      title = title,
      reason = reason,
      action = action,
      background = colors.background(),
    )
  }

private data class FailureScreenParams(
  val reason: String? = null,
  val title: String = "Failed syncing the doodads",
  val action: FailureAction? =
    FailureAction(text = { "Retry" }, icon = MaterialIcons.Refresh, onClick = {}),
  val background: Colors.() -> Color = { cardBackground },
)

private class FailureScreenProvider :
  ColoredParameterProvider<FailureScreenParams>(
    FailureScreenParams(reason = "Some error", background = { buttonPrimaryBackground }),
    FailureScreenParams(
      reason = "Failed to do the thing, here's a bit more text to show how it behaves when wrapping"
    ),
    FailureScreenParams(action = null),
  )
