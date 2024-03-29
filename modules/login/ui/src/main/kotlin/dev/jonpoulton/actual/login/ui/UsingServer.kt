package dev.jonpoulton.actual.login.ui

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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jonpoulton.actual.core.model.ServerUrl
import dev.jonpoulton.actual.core.res.R
import dev.jonpoulton.actual.core.ui.ActualScreenPreview
import dev.jonpoulton.actual.core.ui.BareActualTextButton
import dev.jonpoulton.actual.core.ui.HorizontalSpacer
import dev.jonpoulton.actual.core.ui.LocalActualColorScheme
import dev.jonpoulton.actual.core.ui.PreviewActualColumn

@Stable
@Composable
internal fun UsingServer(
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
      val colorScheme = LocalActualColorScheme.current

      Text(
        text = stringResource(id = R.string.login_using_server),
        fontSize = fontSize,
        color = colorScheme.pageText,
      )

      HorizontalSpacer(width = 5.dp)

      Text(
        text = url.toString(),
        fontSize = fontSize,
        color = colorScheme.pageText,
        fontWeight = FontWeight.Bold,
      )
    }

    BareActualTextButton(
      text = stringResource(id = R.string.login_server_change),
      onClick = onClickChange,
    )
  }
}

@ActualScreenPreview
@Composable
private fun PreviewUsingServer() = PreviewActualColumn {
  UsingServer(
    url = ServerUrl.Demo,
    onClickChange = {},
  )
}
