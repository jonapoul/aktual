package dev.jonpoulton.actual.serverurl.ui

import alakazam.android.ui.compose.PreviewThemes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jonpoulton.actual.core.ui.ActualColorScheme
import dev.jonpoulton.actual.core.ui.ActualFontFamily
import dev.jonpoulton.actual.core.ui.LocalActualColorScheme
import dev.jonpoulton.actual.core.ui.PreviewActual

@Composable
internal fun OkButton(
  isLoading: Boolean = false,
  onClick: () -> Unit,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val colorScheme = LocalActualColorScheme.current

  Button(
    modifier = Modifier.widthIn(min = 1.dp),
    onClick = onClick,
    shape = ButtonShape,
    colors = colorScheme.primaryButton(isPressed),
    contentPadding = ActualButtonPadding,
    interactionSource = interactionSource,
  ) {
    // Using opacity here so we don't adjust the size bounds of the containing box
    Box(contentAlignment = Alignment.Center) {
      OkButtonLoadingContent(
        alpha = if (isLoading) 1f else 0f,
        colorScheme = colorScheme,
      )
      OkButtonTextContent(
        alpha = if (isLoading) 0f else 1f,
      )
    }
  }
}

@Stable
@Composable
private fun ActualColorScheme.primaryButton(isPressed: Boolean): ButtonColors = ButtonDefaults.buttonColors(
  containerColor = if (isPressed) buttonPrimaryBackground else buttonPrimaryBackgroundHover,
  disabledContainerColor = buttonPrimaryDisabledBackground,
  contentColor = if (isPressed) buttonPrimaryText else buttonPrimaryTextHover,
  disabledContentColor = buttonPrimaryDisabledText,
)

@Stable
@Composable
private fun OkButtonTextContent(alpha: Float) {
  Text(
    modifier = Modifier.alpha(alpha),
    text = stringResource(id = android.R.string.ok),
    fontSize = 15.sp,
    fontFamily = ActualFontFamily,
  )
}

@Stable
@Composable
private fun OkButtonLoadingContent(alpha: Float, colorScheme: ActualColorScheme) {
  CircularProgressIndicator(
    modifier = Modifier
      .alpha(alpha)
      .size(20.dp),
    color = colorScheme.buttonPrimaryText,
    strokeWidth = 2.dp,
  )
}

@PreviewThemes
@Composable
private fun PreviewNormal() = PreviewActual {
  OkButton(
    isLoading = false,
    onClick = {},
  )
}

@PreviewThemes
@Composable
private fun PreviewLoading() = PreviewActual {
  OkButton(
    isLoading = true,
    onClick = {},
  )
}
