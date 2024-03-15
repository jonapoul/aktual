package dev.jonpoulton.actual.core.ui

import alakazam.android.ui.compose.PreviewThemes
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun ActualTextField(
  value: String,
  onValueChange: (String) -> Unit,
  placeholderText: String,
  modifier: Modifier = Modifier,
  shape: Shape = ActualTextFieldShape,
  showBorder: Boolean = true,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
  val colorScheme = LocalActualColorScheme.current
  val isFocused by interactionSource.collectIsFocusedAsState()

  var fieldModifier = modifier

  if (showBorder) {
    val borderColor = if (isFocused) colorScheme.formInputBorderSelected else colorScheme.formInputBackgroundSelected
    fieldModifier = fieldModifier.border(1.dp, borderColor, shape)
  }

  if (isFocused) {
    fieldModifier = fieldModifier.shadow(4.dp, shape, ambientColor = colorScheme.formInputShadowSelected)
  }

  TextField(
    modifier = fieldModifier,
    value = value,
    placeholder = { Text(text = placeholderText, fontFamily = ActualFontFamily) },
    shape = shape,
    colors = colorScheme.textField(),
    interactionSource = interactionSource,
    onValueChange = onValueChange,
  )
}

@Composable
fun BigActualTextField(
  value: String,
  onValueChange: (String) -> Unit,
  placeholderText: String,
  modifier: Modifier = Modifier,
  shape: Shape = ActualTextFieldShape,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
  ActualTextField(
    value = value,
    onValueChange = onValueChange,
    placeholderText = placeholderText,
    modifier = modifier,
    shape = shape,
    showBorder = false,
    interactionSource = interactionSource,
  )
}

@Stable
@Composable
private fun ActualColorScheme.textField(): TextFieldColors = TextFieldDefaults.colors(
  focusedTextColor = formInputText,
  unfocusedTextColor = formInputText,
  focusedPlaceholderColor = formInputTextPlaceholder,
  unfocusedPlaceholderColor = formInputTextPlaceholder,
  focusedIndicatorColor = Color.Transparent,
  unfocusedIndicatorColor = Color.Transparent,
  disabledIndicatorColor = Color.Transparent,
  focusedContainerColor = tableBackground,
  unfocusedContainerColor = tableBackground,
  cursorColor = formInputText,
)

private val ActualTextFieldShape = RoundedCornerShape(size = 4.dp)

@PreviewThemes
@Composable
private fun PreviewEmptyTextField() = PreviewActual {
  ActualTextField(
    value = "",
    onValueChange = {},
    placeholderText = "I'm empty",
  )
}

@PreviewThemes
@Composable
private fun PreviewFilledTextField() = PreviewActual {
  ActualTextField(
    value = "I'm full",
    onValueChange = {},
    placeholderText = "Hello world",
  )
}

@PreviewThemes
@Composable
private fun PreviewEmptyBigTextField() = PreviewActual {
  BigActualTextField(
    value = "",
    onValueChange = {},
    placeholderText = "I'm empty",
  )
}

@PreviewThemes
@Composable
private fun PreviewFilledBigTextField() = PreviewActual {
  BigActualTextField(
    value = "I'm full",
    onValueChange = {},
    placeholderText = "Hello world",
  )
}
