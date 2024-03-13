package dev.jonpoulton.actual.serverurl.ui

import alakazam.android.ui.compose.PreviewThemes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.jonpoulton.actual.core.ui.ActualColors
import dev.jonpoulton.actual.core.ui.ActualFontFamily
import dev.jonpoulton.actual.core.ui.PreviewActual

@Composable
internal fun OkButton(
  isEnabled: Boolean = true,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  onClick: () -> Unit,
) {
  val isPressed by interactionSource.collectIsPressedAsState()
  val backgroundColor = if (isPressed) ActualColors.purple600 else ActualColors.purple400

  Button(
    modifier = Modifier.widthIn(min = 1.dp),
    onClick = onClick,
    shape = ButtonShape,
    enabled = isEnabled,
    colors = ButtonDefaults.buttonColors(
      containerColor = backgroundColor,
      contentColor = ActualColors.white,
      disabledContainerColor = ,
      disabledContentColor = ,
    ),
    contentPadding = ActualButtonPadding,
    content = { Text(text = stringResource(id = android.R.string.ok), fontFamily = ActualFontFamily) },
    interactionSource = interactionSource,
  )
}

@PreviewThemes
@Composable
private fun PreviewNormal() = PreviewActual {
  OkButton(
    onClick = {},
  )
}
