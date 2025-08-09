package actual.budget.list.ui

import actual.core.icons.ActualIcons
import actual.core.icons.CloudWarning
import actual.core.ui.LocalTheme
import actual.core.ui.PrimaryTextButton
import actual.core.ui.Theme
import actual.l10n.Strings
import alakazam.kotlin.compose.VerticalSpacer
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
        contentDescription = Strings.budgetFailureDesc,
      )

      VerticalSpacer(30.dp)

      Text(
        text = Strings.budgetFailureMessage,
        color = theme.warningText,
        textAlign = TextAlign.Center,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
      )

      VerticalSpacer(15.dp)

      Text(
        text = reason ?: Strings.budgetFailureDefaultMessage,
        color = theme.warningTextDark,
        fontSize = 16.sp,
        textAlign = TextAlign.Center,
      )

      VerticalSpacer(30.dp)

      val retryText = Strings.budgetFailureRetry
      PrimaryTextButton(
        prefix = { Icon(imageVector = Icons.Filled.Refresh, contentDescription = retryText) },
        text = retryText,
        onClick = onClickRetry,
      )
    }
  }
}
