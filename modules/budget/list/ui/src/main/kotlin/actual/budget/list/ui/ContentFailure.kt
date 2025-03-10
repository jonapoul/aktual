package actual.budget.list.ui

import actual.budget.list.res.BudgetListStrings
import actual.core.icons.ActualIcons
import actual.core.icons.CloudWarning
import actual.core.ui.ActualFontFamily
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewScreen
import actual.core.ui.PrimaryTextButton
import actual.core.ui.ScreenPreview
import actual.core.ui.Theme
import alakazam.android.ui.compose.VerticalSpacer
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ContentFailure(
  reason: String?,
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
        contentDescription = BudgetListStrings.budgetFailureDesc,
      )

      VerticalSpacer(30.dp)

      Text(
        text = BudgetListStrings.budgetFailureMessage,
        color = theme.warningText,
        fontFamily = ActualFontFamily,
        textAlign = TextAlign.Center,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
      )

      VerticalSpacer(15.dp)

      Text(
        text = reason ?: BudgetListStrings.budgetFailureDefaultMessage,
        color = theme.warningTextDark,
        fontFamily = ActualFontFamily,
        fontSize = 16.sp,
        textAlign = TextAlign.Center,
      )

      VerticalSpacer(30.dp)

      val retryText = BudgetListStrings.budgetFailureRetry
      PrimaryTextButton(
        prefix = { Icon(imageVector = Icons.Filled.Refresh, contentDescription = retryText) },
        text = retryText,
        onClick = onClickRetry,
      )
    }
  }
}

@ScreenPreview
@Composable
private fun Failure() = PreviewScreen {
  ContentFailure(
    reason = "Failed to do the thing, here's a bit more text to show how it behaves when wrapping",
    onClickRetry = {},
  )
}

@ScreenPreview
@Composable
private fun NoReason() = PreviewScreen {
  ContentFailure(
    reason = null,
    onClickRetry = {},
  )
}
