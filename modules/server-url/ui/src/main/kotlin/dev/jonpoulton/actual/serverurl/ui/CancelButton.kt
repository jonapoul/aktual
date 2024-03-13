package dev.jonpoulton.actual.serverurl.ui

import alakazam.android.ui.compose.PreviewThemes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.jonpoulton.actual.core.ui.ActualColors
import dev.jonpoulton.actual.core.ui.ActualFontFamily
import dev.jonpoulton.actual.core.ui.PreviewActual

@Composable
internal fun CancelButton(onClick: () -> Unit) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val backgroundColor = if (isPressed) Color(color = 0x4DC8C8C8) else ActualColors.transparent

  TextButton(
    modifier = Modifier
      .widthIn(min = 1.dp),
    onClick = onClick,
    shape = RoundedCornerShape(size = 4.dp),
    colors = ButtonDefaults.buttonColors(
      containerColor = backgroundColor,
      contentColor = ActualColors.navy150,
    ),
    contentPadding = ActualButtonPadding,
    content = { Text(text = "Cancel", fontFamily = ActualFontFamily) },
    interactionSource = interactionSource,
  )
}

@PreviewThemes
@Composable
private fun PreviewNormal() = PreviewActual {
  CancelButton(
    onClick = {},
  )
}
