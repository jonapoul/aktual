package dev.jonpoulton.actual.serverurl.ui

import alakazam.android.ui.compose.PreviewThemes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.jonpoulton.actual.core.ui.ActualColorScheme
import dev.jonpoulton.actual.core.ui.ActualFontFamily
import dev.jonpoulton.actual.core.ui.LocalActualColorScheme
import dev.jonpoulton.actual.core.ui.PreviewActual

@Composable
internal fun CancelButton(onClick: () -> Unit) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val colorScheme = LocalActualColorScheme.current

  TextButton(
    modifier = Modifier.widthIn(min = 1.dp),
    onClick = onClick,
    shape = RoundedCornerShape(size = 4.dp),
    colors = colorScheme.bareButton(isPressed),
    contentPadding = ActualButtonPadding,
    content = { Text(text = "Cancel", fontFamily = ActualFontFamily) },
    interactionSource = interactionSource,
  )
}

@Stable
@Composable
private fun ActualColorScheme.bareButton(isPressed: Boolean): ButtonColors = ButtonDefaults.buttonColors(
  containerColor = if (isPressed) buttonBareBackground else buttonBareBackgroundHover,
  disabledContainerColor = buttonBareDisabledBackground,
  contentColor = if (isPressed) buttonBareText else buttonBareTextHover,
  disabledContentColor = buttonBareDisabledText,
)

@PreviewThemes
@Composable
private fun PreviewNormal() = PreviewActual {
  CancelButton(
    onClick = {},
  )
}
