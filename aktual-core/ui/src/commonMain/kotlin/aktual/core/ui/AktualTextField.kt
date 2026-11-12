package aktual.core.ui

import aktual.core.icons.AktualIcons
import aktual.core.icons.Delete
import aktual.core.l10n.Strings
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
  keyboardOptions: KeyboardOptions = Default,
  onKeyboardAction: KeyboardActionHandler? = null,
  colors: TextFieldColors = AktualTheme.colors.textField(),
  clearable: Boolean = false,
  textStyle: TextStyle = LocalTextStyle.current,
  showBorder: Boolean = true,
  supportingText: (@Composable () -> Unit)? = null,
  isError: Boolean = false,
) {
  val isFocused by interactionSource.collectIsFocusedAsState()
  val borderColor =
    if (isError) {
      AktualTheme.colors.errorBorder
    } else if (isFocused) {
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
      val shadowColor =
        if (isError) AktualTheme.colors.errorBorder else AktualTheme.colors.formInputShadowSelected
      Modifier.shadow(4.dp, shape, ambientColor = shadowColor)
    } else {
      Modifier
    }

  val focusRequester = remember { FocusRequester() }
  val keyboard = LocalSoftwareKeyboardController.current

  val clearButton: (@Composable () -> Unit)? =
    if (clearable && state.text.isNotEmpty()) {
      {
        ClearButton(
          tint = colors.focusedTrailingIconColor,
          onClick = {
            state.edit { replace(0, length, "") }
            // The field may still be focused with the keyboard hidden, so show it explicitly
            focusRequester.requestFocus()
            keyboard?.show()
          },
        )
      }
    } else {
      null
    }

  // Supporting text sits outside the bordered field. Intrinsic width keeps the field's natural
  // size when the caller doesn't set one
  Column(modifier.width(IntrinsicSize.Max)) {
    TextField(
      modifier =
        Modifier.fillMaxWidth()
          .then(borderModifier)
          .then(shadowModifier)
          .focusRequester(focusRequester),
      state = state,
      placeholder =
        placeholderText?.let {
          { Text(text = it, textAlign = textStyle.textAlign, modifier = Modifier.fillMaxWidth()) }
        },
      shape = shape,
      colors = colors,
      readOnly = readOnly,
      enabled = isEnabled,
      lineLimits = if (singleLine) SingleLine else Default,
      leadingIcon = leadingIcon,
      trailingIcon = trailingIcon ?: clearButton,
      interactionSource = interactionSource,
      outputTransformation = outputTransformation,
      keyboardOptions = keyboardOptions,
      onKeyboardAction = onKeyboardAction,
      textStyle = textStyle,
      isError = isError,
    )

    if (supportingText != null) {
      val supportingColor =
        when {
          !isEnabled -> colors.disabledSupportingTextColor
          isError -> colors.errorSupportingTextColor
          isFocused -> colors.focusedSupportingTextColor
          else -> colors.unfocusedSupportingTextColor
        }
      CompositionLocalProvider(
        LocalContentColor provides supportingColor,
        LocalTextStyle provides AktualTheme.typography.bodySmall,
      ) {
        Box(Modifier.padding(start = 16.dp, top = 4.dp, end = 16.dp)) { supportingText() }
      }
    }
  }
}

@Composable
private fun ClearButton(tint: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
  IconButton(modifier = modifier, onClick = onClick) {
    Icon(imageVector = AktualIcons.Delete, contentDescription = Strings.inputClear, tint = tint)
  }
}

private data class TextInputPreviewParams(
  val initialText: String,
  val clearable: Boolean = false,
  val isError: Boolean = false,
)

private class TextInputPreviewProvider :
  ColorParamsProvider<TextInputPreviewParams>(
    TextInputPreviewParams(initialText = ""),
    TextInputPreviewParams(initialText = "I'm full"),
    TextInputPreviewParams(initialText = "I'm full", clearable = true),
    TextInputPreviewParams(initialText = "I'm wrong", isError = true),
  )

@Preview
@Composable
private fun PreviewAktualTextField(
  @PreviewParameter(TextInputPreviewProvider::class) params: ColorParams<TextInputPreviewParams>,
) =
  PreviewWithColorParams(params) {
    AktualTextField(
      state = rememberTextFieldState(initialText = initialText),
      placeholderText = "Placeholder",
      clearable = clearable,
      isError = isError,
      supportingText =
        if (isError) {
          { Text("Something went wrong") }
        } else {
          null
        },
    )
  }
