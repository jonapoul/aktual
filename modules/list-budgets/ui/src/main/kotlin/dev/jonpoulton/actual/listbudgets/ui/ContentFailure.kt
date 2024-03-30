package dev.jonpoulton.actual.listbudgets.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jonpoulton.actual.core.icons.ActualIcons
import dev.jonpoulton.actual.core.icons.CloudWarning
import dev.jonpoulton.actual.core.ui.ActualColorScheme
import dev.jonpoulton.actual.core.ui.ActualFontFamily
import dev.jonpoulton.actual.core.ui.ActualScreenPreview
import dev.jonpoulton.actual.core.ui.LocalActualColorScheme
import dev.jonpoulton.actual.core.ui.PreviewActualScreen
import dev.jonpoulton.actual.core.ui.PrimaryActualTextButton
import dev.jonpoulton.actual.core.ui.VerticalSpacer
import dev.jonpoulton.actual.core.res.R as ResR

@Composable
internal fun ContentFailure(
  reason: String,
  onClickRetry: () -> Unit,
  modifier: Modifier = Modifier,
  colors: ActualColorScheme = LocalActualColorScheme.current,
) {
  Box(
    modifier = modifier
      .padding(20.dp)
      .fillMaxWidth(),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Icon(
        modifier = Modifier.size(100.dp),
        imageVector = ActualIcons.CloudWarning,
        tint = colors.warningText,
        contentDescription = stringResource(ResR.string.budget_failure_desc),
      )

      VerticalSpacer(30.dp)

      Text(
        text = stringResource(id = ResR.string.budget_failure_message),
        color = colors.warningText,
        fontFamily = ActualFontFamily,
        textAlign = TextAlign.Center,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
      )

      VerticalSpacer(15.dp)

      Text(
        text = reason,
        color = colors.warningTextDark,
        fontFamily = ActualFontFamily,
        fontSize = 16.sp,
        textAlign = TextAlign.Center,
      )

      VerticalSpacer(30.dp)

      val retryText = stringResource(id = ResR.string.budget_failure_retry)
      PrimaryActualTextButton(
        prefix = { Icon(imageVector = Icons.Filled.Refresh, contentDescription = retryText) },
        text = retryText,
        onClick = onClickRetry,
      )
    }
  }
}

@ActualScreenPreview
@Composable
private fun Failure() = PreviewActualScreen {
  ContentFailure(
    reason = "Failed to do the thing, here's a bit more text to show how it behaves when wrapping",
    onClickRetry = {},
  )
}
