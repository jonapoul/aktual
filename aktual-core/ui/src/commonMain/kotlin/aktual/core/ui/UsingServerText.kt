package aktual.core.ui

import aktual.core.l10n.Strings
import aktual.core.model.ServerUrl
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Stable
@Composable
fun UsingServerText(
  url: ServerUrl?,
  onClickChange: () -> Unit,
  modifier: Modifier = Modifier,
  fontSize: TextUnit = 16.sp,
  theme: Theme = LocalTheme.current,
) {
  Column(
    modifier = modifier
      .aktualHaze()
      .background(Color.Transparent, RounderCardShape)
      .padding(20.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = Strings.loginUsingServer,
      fontSize = fontSize,
      color = theme.pageText,
      textAlign = TextAlign.Center,
    )

    Text(
      text = url?.toString().orEmpty(),
      fontSize = fontSize,
      color = theme.pageText,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center,
    )

    BareTextButton(
      text = Strings.loginServerChange,
      onClick = onClickChange,
    )
  }
}

@Preview
@Composable
private fun PreviewUsingServerText(
  @PreviewParameter(ServerUrlProvider::class) params: ThemedParams<ServerUrl?>,
) = PreviewWithColorScheme(params.type) {
  UsingServerText(url = params.data, onClickChange = {})
}

private class ServerUrlProvider : ThemedParameterProvider<ServerUrl?>(ServerUrl.Demo, null)
