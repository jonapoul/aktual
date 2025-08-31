package actual.app.android

import actual.core.icons.ActualIcons
import actual.core.icons.CloudCheck
import actual.core.icons.CloudWarning
import actual.core.model.PingState
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewThemedColumn
import actual.core.ui.Theme
import actual.l10n.Strings
import alakazam.kotlin.compose.HorizontalSpacer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun BottomStatusBar(
  state: BottomBarState.Visible,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Row(
  modifier = modifier
    .background(theme.cardBackground)
    .padding(vertical = 3.dp, horizontal = 8.dp)
    .fillMaxWidth(),
  verticalAlignment = Alignment.CenterVertically,
) {
  val (pingState, budgetName) = state

  if (budgetName != null) {
    Text(
      text = loadedString(budgetName),
      fontSize = 10.sp,
      color = theme.pageText,
    )
  }

  val text = pingState.text()
  val tint = pingState.tint(theme)

  HorizontalSpacer(weight = 1f)

  Image(
    modifier = Modifier.size(12.dp),
    imageVector = pingState.icon(),
    contentDescription = text,
    colorFilter = ColorFilter.tint(tint),
  )

  HorizontalSpacer(5.dp)

  Text(
    text = text,
    fontSize = 10.sp,
    color = tint,
  )
}

@Stable
private fun PingState.icon() = when (this) {
  PingState.Success -> ActualIcons.CloudCheck
  PingState.Failure -> ActualIcons.CloudWarning
  PingState.Unknown -> ActualIcons.CloudWarning
}

@Composable
private fun PingState.text() = when (this) {
  PingState.Success -> Strings.connectionConnected
  PingState.Failure -> Strings.connectionDisconnected
  PingState.Unknown -> Strings.connectionUnknown
}

@Stable
private fun PingState.tint(theme: Theme) = when (this) {
  PingState.Success -> theme.successText
  PingState.Failure -> theme.warningText
  PingState.Unknown -> theme.pageTextSubdued
}

@Stable
@Composable
private fun loadedString(budgetName: String): AnnotatedString = buildAnnotatedString {
  append(Strings.budgetLoaded)
  append("  ")
  withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
    append(budgetName)
  }
}

@Preview
@Composable
private fun PreviewConnected() = PreviewThemedColumn {
  BottomStatusBar(
    state = BottomBarState.Visible(
      pingState = PingState.Success,
      budgetName = "My Budget",
    ),
  )
}

@Preview
@Composable
private fun PreviewDisconnected() = PreviewThemedColumn {
  BottomStatusBar(
    state = BottomBarState.Visible(
      pingState = PingState.Failure,
      budgetName = null,
    ),
  )
}

@Preview
@Composable
private fun PreviewUnknown() = PreviewThemedColumn {
  BottomStatusBar(
    state = BottomBarState.Visible(
      pingState = PingState.Unknown,
      budgetName = null,
    ),
  )
}
