package dev.jonpoulton.actual.core.ui

import alakazam.android.ui.compose.PreviewThemes
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ActualTextInput(
  value: String,
  onValueChange: (String) -> Unit,
  placeholderText: String,
  modifier: Modifier = Modifier,
  fontSize: TextUnit = TextUnit.Unspecified,
  showBorder: Boolean = true,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
  val colorScheme = LocalActualColorScheme.current
  val isFocused by interactionSource.collectIsFocusedAsState()

  var fieldModifier = modifier

  if (showBorder) {
    val borderColor = if (isFocused) colorScheme.formInputBorderSelected else colorScheme.formInputBackgroundSelected
    fieldModifier = fieldModifier.border(width = 1.dp, color = borderColor, shape = TextFieldShape)
  }

  if (isFocused) {
    fieldModifier = fieldModifier.shadow(4.dp, TextFieldShape, ambientColor = colorScheme.formInputShadowSelected)
  }

  TextField(
    modifier = fieldModifier,
    value = value,
    placeholder = { Text(text = placeholderText, fontFamily = ActualFontFamily) },
    shape = TextFieldShape,
    colors = colorScheme.textField(),
    interactionSource = interactionSource,
    textStyle = TextStyle.Default.copy(fontSize = fontSize),
    onValueChange = onValueChange,
  )
}

@Composable
fun BigActualTextInput(
  value: String,
  onValueChange: (String) -> Unit,
  placeholderText: String,
  modifier: Modifier = Modifier,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
  ActualTextInput(
    value = value,
    onValueChange = onValueChange,
    placeholderText = placeholderText,
    modifier = modifier.padding(all = 10.dp),
    fontSize = 15.sp,
    showBorder = false,
    interactionSource = interactionSource,
  )
}

@Stable
@Composable
private fun ActualColorScheme.textField(): TextFieldColors = TextFieldDefaults.colors(
  focusedTextColor = formInputText,
  unfocusedTextColor = formInputText,
  focusedPlaceholderColor = formInputTextPlaceholderSelected,
  unfocusedPlaceholderColor = formInputTextPlaceholder,
  focusedIndicatorColor = Color.Transparent,
  unfocusedIndicatorColor = Color.Transparent,
  disabledIndicatorColor = Color.Transparent,
  focusedContainerColor = tableBackground,
  unfocusedContainerColor = tableBackground,
  cursorColor = formInputText,
)

private val TextFieldShape = RoundedCornerShape(size = 8.dp)

@PreviewThemes
@Composable
private fun PreviewEmptyTextInput() = PreviewActual {
  ActualTextInput(
    value = "",
    onValueChange = {},
    placeholderText = "I'm empty",
  )
}

@PreviewThemes
@Composable
private fun PreviewFilledTextInput() = PreviewActual {
  ActualTextInput(
    value = "I'm full",
    onValueChange = {},
    placeholderText = "Hello world",
  )
}

@PreviewThemes
@Composable
private fun PreviewEmptyBigTextInput() = PreviewActual {
  BigActualTextInput(
    value = "",
    onValueChange = {},
    placeholderText = "I'm empty",
  )
}

@PreviewThemes
@Composable
private fun PreviewFilledBigTextInput() = PreviewActual {
  BigActualTextInput(
    value = "I'm full",
    onValueChange = {},
    placeholderText = "Hello world",
  )
}
