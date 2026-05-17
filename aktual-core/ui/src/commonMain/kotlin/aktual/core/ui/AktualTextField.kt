package aktual.core.ui

import aktual.core.icons.material.Clear
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
fun AktualTextField(
  value: String,
  onValueChange: (String) -> Unit,
  placeholderText: String?,
  modifier: Modifier = Modifier,
  shape: Shape = TextFieldShape,
  readOnly: Boolean = false,
  isEnabled: Boolean = true,
  singleLine: Boolean = false,
  leadingIcon: (@Composable () -> Unit)? = null,
  trailingIcon: (@Composable () -> Unit)? = null,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  visualTransformation: VisualTransformation = VisualTransformation.None,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  theme: Theme = LocalTheme.current,
  colors: TextFieldColors = theme.textField(),
  clearable: Boolean = false,
  textStyle: TextStyle = LocalTextStyle.current,
) {
  val isFocused by interactionSource.collectIsFocusedAsState()
  val borderColor =
    if (isFocused) theme.formInputBorderSelected else theme.formInputBackgroundSelected

  var fieldModifier = modifier.border(1.dp, borderColor, shape)

  if (isFocused) {
    fieldModifier = fieldModifier.shadow(4.dp, shape, ambientColor = theme.formInputShadowSelected)
  }

  val clearButton: (@Composable () -> Unit)? =
    if (clearable && value.isNotEmpty()) {
      { ClearButton(tint = colors.focusedTrailingIconColor, onClick = { onValueChange("") }) }
    } else {
      null
    }

  TextField(
    modifier = fieldModifier,
    value = value,
    placeholder = placeholderText?.let { { Text(text = it) } },
    shape = shape,
    colors = colors,
    readOnly = readOnly,
    enabled = isEnabled,
    singleLine = singleLine,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon ?: clearButton,
    interactionSource = interactionSource,
    visualTransformation = visualTransformation,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    onValueChange = onValueChange,
    textStyle = textStyle,
  )
}

@Composable
private fun ClearButton(tint: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
  IconButton(modifier = modifier, onClick = onClick) {
    Icon(imageVector = MaterialIcons.Clear, contentDescription = Strings.inputClear, tint = tint)
  }
}

private data class TextInputPreviewParams(
  val value: String,
  val placeholderText: String = "Placeholder",
  val isClearable: Boolean = false,
)

private class TextInputPreviewProvider :
  ThemedParameterProvider<TextInputPreviewParams>(
    TextInputPreviewParams(value = ""),
    TextInputPreviewParams(value = "I'm full"),
    TextInputPreviewParams(value = "I'm full", isClearable = true),
  )

@Preview
@Composable
private fun PreviewAktualTextField(
  @PreviewParameter(TextInputPreviewProvider::class) params: ThemedParams<TextInputPreviewParams>
) =
  PreviewWithTheme(params.theme) {
    AktualTextField(
      value = params.data.value,
      onValueChange = {},
      placeholderText = params.data.placeholderText,
      clearable = params.data.isClearable,
    )
  }
