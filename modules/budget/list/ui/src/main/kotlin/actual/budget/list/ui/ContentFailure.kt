package actual.budget.list.ui

import actual.core.icons.ActualIcons
import actual.core.icons.CloudWarning
import actual.core.ui.ActualFontFamily
import actual.core.ui.PreviewActualScreen
import actual.core.ui.ActualScreenPreview
import actual.core.ui.LocalTheme
import actual.core.ui.PrimaryActualTextButton
import actual.core.ui.Theme
import actual.core.ui.VerticalSpacer
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
import actual.core.res.R as CoreR

@Composable
internal fun ContentFailure(
  reason: String,
  onClickRetry: () -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
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
        tint = theme.warningText,
        contentDescription = stringResource(CoreR.string.budget_failure_desc),
      )

      VerticalSpacer(30.dp)

      Text(
        text = stringResource(id = CoreR.string.budget_failure_message),
        color = theme.warningText,
        fontFamily = ActualFontFamily,
        textAlign = TextAlign.Center,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
      )

      VerticalSpacer(15.dp)

      Text(
        text = reason,
        color = theme.warningTextDark,
        fontFamily = ActualFontFamily,
        fontSize = 16.sp,
        textAlign = TextAlign.Center,
      )

      VerticalSpacer(30.dp)

      val retryText = stringResource(id = CoreR.string.budget_failure_retry)
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
