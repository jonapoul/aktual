package aktual.core.ui

import aktual.core.icons.AktualIcons
import aktual.core.icons.CloudWarning
import aktual.core.icons.MaterialIcons
import aktual.core.icons.Refresh
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
    reason: String,
    retryText: String,
    onClickRetry: () -> Unit,
    modifier: Modifier = Modifier,
    theme: Theme = LocalTheme.current,
) =
    Box(
        modifier = modifier.padding(20.dp).fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
      Column(
          modifier =
              Modifier.padding(Dimens.Small)
                  .background(Color.Transparent, CardShape)
                  .aktualHaze()
                  .padding(30.dp),
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

        VerticalSpacer(15.dp)

        Text(
            text = reason,
            color = theme.warningTextDark,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
        )

        VerticalSpacer(30.dp)

        PrimaryTextButton(
            prefix = { Icon(imageVector = MaterialIcons.Refresh, contentDescription = retryText) },
            text = retryText,
            onClick = onClickRetry,
        )
      }
    }

@PortraitPreview
@Composable
private fun PreviewFailureScreen(
    @PreviewParameter(FailureScreenProvider::class) params: ThemedParams<String>,
) =
    PreviewWithColorScheme(params.type) {
      FailureScreen(
          title = "Failed syncing the doodads",
          reason = params.data,
          retryText = "Retry",
          onClickRetry = {},
      )
    }

private class FailureScreenProvider :
    ThemedParameterProvider<String?>(
        "Some error",
        "Failed to do the thing, here's a bit more text to show how it behaves when wrapping",
    )
