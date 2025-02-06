package actual.core.ui

import actual.core.model.ServerUrl
import actual.core.res.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Stable
@Composable
fun UsingServerText(
  url: ServerUrl,
  onClickChange: () -> Unit,
  modifier: Modifier = Modifier,
  fontSize: TextUnit = 16.sp,
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
    ) {
      val theme = LocalTheme.current

      Text(
        text = stringResource(id = R.string.login_using_server),
        fontSize = fontSize,
        color = theme.pageText,
      )

      HorizontalSpacer(width = 5.dp)

      Text(
        text = url.toString(),
        fontSize = fontSize,
        color = theme.pageText,
        fontWeight = FontWeight.Bold,
      )
    }

    BareActualTextButton(
      text = stringResource(id = R.string.login_server_change),
      onClick = onClickChange,
    )
  }
}

@Preview
@Composable
private fun PreviewUsingServer() = PreviewActualColumn {
  UsingServerText(
    url = ServerUrl.Demo,
    onClickChange = {},
  )
}
