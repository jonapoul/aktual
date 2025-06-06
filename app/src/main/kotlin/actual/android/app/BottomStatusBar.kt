package actual.android.app

import actual.core.icons.ActualIcons
import actual.core.icons.CloudCheck
import actual.core.icons.CloudWarning
import actual.core.res.CoreStrings
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewColumn
import actual.core.ui.Theme
import alakazam.android.ui.compose.HorizontalSpacer
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
    .background(theme.cardBackground.copy(alpha = 0.8f))
    .padding(vertical = 3.dp, horizontal = 8.dp)
    .fillMaxWidth(),
  verticalAlignment = Alignment.CenterVertically,
) {
  if (state.budgetName != null) {
    Text(
      text = loadedString(state.budgetName),
      fontSize = 10.sp,
      color = theme.pageText,
    )
  }

  val text = text(state.isConnected)
  val tint = tint(state.isConnected, theme)

  HorizontalSpacer(weight = 1f)

  Image(
    modifier = Modifier.size(12.dp),
    imageVector = icon(state.isConnected),
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
private fun icon(isConnected: Boolean) = when (isConnected) {
  true -> ActualIcons.CloudCheck
  false -> ActualIcons.CloudWarning
}

@Stable
@Composable
private fun text(isConnected: Boolean) = when (isConnected) {
  true -> CoreStrings.connectionConnected
  false -> CoreStrings.connectionDisconnected
}

@Stable
private fun tint(isConnected: Boolean, theme: Theme) = when (isConnected) {
  true -> theme.successText
  false -> theme.warningText
}

@Stable
@Composable
private fun loadedString(budgetName: String): AnnotatedString = buildAnnotatedString {
  append(CoreStrings.budgetLoaded)
  append("  ")
  withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
    append(budgetName)
  }
}

@Preview
@Composable
private fun PreviewConnected() = PreviewColumn {
  BottomStatusBar(
    state = BottomBarState.Visible(
      isConnected = true,
      budgetName = "My Budget",
    ),
  )
}

@Preview
@Composable
private fun PreviewDisconnected() = PreviewColumn {
  BottomStatusBar(
    state = BottomBarState.Visible(
      isConnected = false,
      budgetName = null,
    ),
  )
}
