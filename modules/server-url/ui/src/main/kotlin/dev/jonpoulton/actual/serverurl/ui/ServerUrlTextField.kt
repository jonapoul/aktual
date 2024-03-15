package dev.jonpoulton.actual.serverurl.ui

import alakazam.android.ui.compose.PreviewThemes
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.jonpoulton.actual.core.ui.ActualColorScheme
import dev.jonpoulton.actual.core.ui.LocalActualColorScheme
import dev.jonpoulton.actual.core.ui.PreviewActual

@Composable
internal fun ServerUrlTextField(
  url: String,
  onUrlEntered: (String) -> Unit,
) {
  val colorScheme = LocalActualColorScheme.current
  val interactionSource = remember { MutableInteractionSource() }
  val isFocused by interactionSource.collectIsFocusedAsState()
  val borderColor = if (isFocused) colorScheme.formInputBorderSelected else colorScheme.formInputBorder

  TextField(
    modifier = Modifier
      .fillMaxWidth()
      .border(width = 1.dp, color = borderColor, shape = TextFieldShape)
      .shadow(elevation = 4.dp, shape = TextFieldShape, ambientColor = colorScheme.formInputShadowSelected),
    value = url,
    placeholder = { Text(text = EXAMPLE_URL) },
    shape = TextFieldShape,
    colors = colorScheme.textField(),
    interactionSource = interactionSource,
    onValueChange = onUrlEntered,
  )
}

private const val EXAMPLE_URL = "https://example.com"

private val TextFieldShape = RoundedCornerShape(size = 8.dp)

@Stable
@Composable
private fun ActualColorScheme.textField(): TextFieldColors = TextFieldDefaults.colors(
  focusedTextColor = formInputTextSelected,
  unfocusedTextColor = formInputText,
  focusedPlaceholderColor = formInputTextPlaceholderSelected,
  unfocusedPlaceholderColor = formInputTextPlaceholder,
  focusedIndicatorColor = Color.Transparent,
  unfocusedIndicatorColor = Color.Transparent,
  disabledIndicatorColor = Color.Transparent,
)

@PreviewThemes
@Composable
private fun PreviewWithUrl() = PreviewActual {
  ServerUrlTextField(
    url = "https://my.website.com:1234/actual",
    onUrlEntered = {},
  )
}

@PreviewThemes
@Composable
private fun PreviewEmpty() = PreviewActual {
  ServerUrlTextField(
    url = "",
    onUrlEntered = {},
  )
}
