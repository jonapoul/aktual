package aktual.core.ui

import aktual.core.icons.AktualIcons
import aktual.core.icons.CloudWarning
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Refresh
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FailureScreen(
  title: String,
  reason: String?,
  retryText: String?,
  onClickRetry: (() -> Unit)?,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Box(modifier = modifier.padding(20.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
    Column(
      modifier =
        Modifier.padding(Dimens.Small).background(Color.Transparent, CardShape).padding(30.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Icon(
        modifier = Modifier.size(100.dp),
        imageVector = AktualIcons.CloudWarning,
        tint = theme.warningText,
        contentDescription = title,
      )

      VerticalSpacer(30.dp)

      Text(
        text = title,
        color = theme.warningText,
        textAlign = TextAlign.Center,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
      )

      if (reason != null) {
        VerticalSpacer(15.dp)

        Text(
          text = reason,
          color = theme.warningTextDark,
          fontSize = 16.sp,
          textAlign = TextAlign.Center,
        )
      }

      if (retryText != null && onClickRetry != null) {
        VerticalSpacer(30.dp)

        PrimaryTextButton(
          prefix = { Icon(imageVector = MaterialIcons.Refresh, contentDescription = retryText) },
          text = retryText,
          onClick = onClickRetry,
        )
      }
    }
  }
}

@PortraitPreview
@Composable
private fun PreviewFailureScreen(
  @PreviewParameter(FailureScreenProvider::class) params: ThemedParams<FailureScreenParams>
) =
  PreviewWithColorScheme(params.theme) {
    FailureScreen(
      title = params.data.title,
      reason = params.data.reason,
      retryText = params.data.retryText,
      onClickRetry = {},
    )
  }

private data class FailureScreenParams(
  val reason: String? = null,
  val title: String = "Failed syncing the doodads",
  val retryText: String? = "Retry",
)

private class FailureScreenProvider :
  ThemedParameterProvider<FailureScreenParams>(
    FailureScreenParams(reason = "Some error"),
    FailureScreenParams(
      reason = "Failed to do the thing, here's a bit more text to show how it behaves when wrapping"
    ),
    FailureScreenParams(retryText = null),
  )
