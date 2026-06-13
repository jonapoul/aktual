package aktual.core.ui

import aktual.core.icons.material.Clear
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
fun AktualTextField(
  state: TextFieldState,
  placeholderText: String?,
  modifier: Modifier = Modifier,
  shape: Shape = TextFieldShape,
  readOnly: Boolean = false,
  isEnabled: Boolean = true,
  singleLine: Boolean = false,
  leadingIcon: (@Composable () -> Unit)? = null,
  trailingIcon: (@Composable () -> Unit)? = null,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  outputTransformation: OutputTransformation? = null,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  onKeyboardAction: KeyboardActionHandler? = null,
  colors: TextFieldColors = AktualTheme.colors.textField(),
  clearable: Boolean = false,
  textStyle: TextStyle = LocalTextStyle.current,
  showBorder: Boolean = true,
  supportingText: (@Composable () -> Unit)? = null,
) {
  val isFocused by interactionSource.collectIsFocusedAsState()
  val borderColor =
    if (isFocused) {
      AktualTheme.colors.formInputBorderSelected
    } else {
      AktualTheme.colors.formInputBackgroundSelected
    }

  val borderModifier =
    if (showBorder) {
      Modifier.border(1.dp, borderColor, shape)
    } else {
      Modifier
    }

  val shadowModifier =
    if (isFocused && showBorder) {
      Modifier.shadow(4.dp, shape, ambientColor = AktualTheme.colors.formInputShadowSelected)
    } else {
      Modifier
    }

  val clearButton: (@Composable () -> Unit)? =
    if (clearable && state.text.isNotEmpty()) {
      {
        ClearButton(
          tint = colors.focusedTrailingIconColor,
          onClick = { state.edit { replace(0, length, "") } },
        )
      }
    } else {
      null
    }

  TextField(
    modifier = modifier.then(borderModifier).then(shadowModifier),
    state = state,
    placeholder =
      placeholderText?.let {
        { Text(text = it, textAlign = textStyle.textAlign, modifier = Modifier.fillMaxWidth()) }
      },
    shape = shape,
    colors = colors,
    readOnly = readOnly,
    enabled = isEnabled,
    lineLimits = if (singleLine) TextFieldLineLimits.SingleLine else TextFieldLineLimits.Default,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon ?: clearButton,
    interactionSource = interactionSource,
    outputTransformation = outputTransformation,
    keyboardOptions = keyboardOptions,
    onKeyboardAction = onKeyboardAction,
    textStyle = textStyle,
    supportingText = supportingText,
  )
}

@Composable
private fun ClearButton(tint: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
  IconButton(modifier = modifier, onClick = onClick) {
    Icon(imageVector = MaterialIcons.Clear, contentDescription = Strings.inputClear, tint = tint)
  }
}

private data class TextInputPreviewParams(val initialText: String, val clearable: Boolean = false)

private class TextInputPreviewProvider :
  ColoredParameterProvider<TextInputPreviewParams>(
    TextInputPreviewParams(initialText = ""),
    TextInputPreviewParams(initialText = "I'm full"),
    TextInputPreviewParams(initialText = "I'm full", clearable = true),
  )

@Preview
@Composable
private fun PreviewAktualTextField(
  @PreviewParameter(TextInputPreviewProvider::class) params: ColoredParams<TextInputPreviewParams>
) =
  PreviewWithColoredParams(params) {
    AktualTextField(
      state = rememberTextFieldState(initialText = initialText),
      placeholderText = "Placeholder",
      clearable = clearable,
    )
  }
