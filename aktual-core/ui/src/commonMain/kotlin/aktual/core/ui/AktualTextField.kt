package aktual.core.ui

import aktual.core.icons.AktualIcons
import aktual.core.icons.Delete
import aktual.core.l10n.Strings
import aktual.theme.Colors
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
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
  inputTransformation: InputTransformation? = null,
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

  val textColor =
    textStyle.color.takeOrElse {
      when {
        !isEnabled -> colors.disabledTextColor
        isError -> colors.errorTextColor
        isFocused -> colors.focusedTextColor
        else -> colors.unfocusedTextColor
      }
    }
  val lineLimits: TextFieldLineLimits = if (singleLine) SingleLine else Default
  val errorDescription = Strings.inputInvalid

  // Built on BasicTextField rather than TextField so the border only wraps the container, leaving
  // the supporting text below it. The icon slots apply the minimum interactive size, so cap it at
  // the field height to stop icons stretching the field
  CompositionLocalProvider(
    LocalTextSelectionColors provides colors.textSelectionColors,
    LocalMinimumInteractiveComponentSize provides TEXT_FIELD_MIN_HEIGHT,
  ) {
    BasicTextField(
      modifier =
        modifier
          .semantics { if (isError) error(errorDescription) }
          .defaultMinSize(TextFieldDefaults.MinWidth, TEXT_FIELD_MIN_HEIGHT)
          .focusRequester(focusRequester),
      state = state,
      enabled = isEnabled,
      readOnly = readOnly,
      textStyle = textStyle.merge(color = textColor),
      cursorBrush = SolidColor(if (isError) colors.errorCursorColor else colors.cursorColor),
      keyboardOptions = keyboardOptions,
      onKeyboardAction = onKeyboardAction,
      lineLimits = lineLimits,
      interactionSource = interactionSource,
      inputTransformation = inputTransformation,
      outputTransformation = outputTransformation,
      decorator =
        TextFieldDefaults.decorator(
          state = state,
          enabled = isEnabled,
          lineLimits = lineLimits,
          outputTransformation = outputTransformation,
          interactionSource = interactionSource,
          placeholder =
            placeholderText?.let { text ->
              {
                Text(
                  text = text,
                  textAlign = textStyle.textAlign,
                  modifier = Modifier.fillMaxWidth(),
                )
              }
            },
          leadingIcon = leadingIcon,
          trailingIcon = trailingIcon ?: clearButton,
          supportingText = supportingText,
          isError = isError,
          colors = colors,
          contentPadding = TEXT_FIELD_CONTENT_PADDING,
          container = {
            TextFieldDefaults.Container(
              modifier =
                Modifier.outline(AktualTheme.colors, showBorder, isFocused, isError, shape),
              enabled = isEnabled,
              isError = isError,
              interactionSource = interactionSource,
              colors = colors,
              shape = shape,
            )
          },
        ),
    )
  }
}

internal val TEXT_FIELD_MIN_HEIGHT = 40.dp

internal val TEXT_FIELD_CONTENT_PADDING =
  TextFieldDefaults.contentPaddingWithoutLabel(
    start = Dimens.VeryLarge,
    end = Dimens.VeryLarge,
    top = Dimens.Large,
    bottom = Dimens.Large,
  )

private fun Modifier.outline(
  colors: Colors,
  showBorder: Boolean,
  isFocused: Boolean,
  isError: Boolean,
  shape: Shape,
): Modifier {
  if (!showBorder) return this
  val borderColor =
    when {
      isError -> colors.errorBorder
      isFocused -> colors.formInputBorderSelected
      else -> colors.formInputBackgroundSelected
    }
  val bordered = border(1.dp, borderColor, shape)
  if (!isFocused) return bordered
  val shadowColor = if (isError) colors.errorBorder else colors.formInputShadowSelected
  return bordered.shadow(4.dp, shape, ambientColor = shadowColor)
}

@Composable
private fun ClearButton(tint: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
  BasicIconButton(
    modifier = modifier.size(CLEAR_BUTTON_SIZE),
    size = CLEAR_ICON_SIZE,
    imageVector = AktualIcons.Delete,
    contentDescription = Strings.inputClear,
    colors =
      IconButtonColorProvider { theme, isPressed ->
        theme.bareIconButton(isPressed).copy(contentColor = tint)
      },
    onClick = onClick,
  )
}

private val CLEAR_BUTTON_SIZE = 32.dp
private val CLEAR_ICON_SIZE = 18.dp

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
