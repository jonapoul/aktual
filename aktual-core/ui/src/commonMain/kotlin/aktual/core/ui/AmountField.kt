package aktual.core.ui

import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.textSizes
import aktual.core.ui.AktualTheme.typography
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed

/**
 * A large text field for typing an amount. With [arithmetic], a row of + − × ÷ keys sits beneath
 * it, since a decimal keyboard has none of them. Read the text with `parseAmountInput` or
 * `evaluateAmountInput`.
 */
@Composable
fun AmountField(
  state: TextFieldState,
  modifier: Modifier = Modifier,
  label: String? = null,
  arithmetic: Boolean = false,
  onKeyboardAction: KeyboardActionHandler? = null,
) {
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AmountFieldDS.spacing)) {
    if (label != null) {
      Text(text = label, style = typography.labelLarge)
    }

    AktualTextField(
      modifier = Modifier.fillMaxWidth(),
      state = state,
      placeholderText = ZERO_PLACEHOLDER,
      singleLine = true,
      keyboardOptions =
        KeyboardOptions(
          keyboardType = Decimal,
          imeAction = if (onKeyboardAction != null) Done else Default,
        ),
      onKeyboardAction = onKeyboardAction,
      textStyle = typography.headlineSmall.copy(color = colors.pageText),
    )

    if (arithmetic) {
      OperatorKeys(onKey = { symbol -> state.edit { append(symbol) } })
    }
  }
}

@Composable
private fun OperatorKeys(onKey: (String) -> Unit, modifier: Modifier = Modifier) {
  val haptics = LocalHapticFeedback.current
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(AmountFieldDS.keySpacing),
  ) {
    operatorKeys().fastForEach { (symbol, description) ->
      NormalTextButton(
        modifier = Modifier.weight(1f).semantics { contentDescription = description },
        text = symbol,
        fontSize = AmountFieldDS.keyFontSize,
        onClick = {
          haptics.performHapticFeedback(KeyboardTap)
          onKey(symbol)
        },
      )
    }
  }
}

@Composable
private fun operatorKeys(): List<Pair<String, String>> =
  listOf(
    "+" to Strings.inputAdd,
    "−" to Strings.inputSubtract,
    "×" to Strings.inputMultiply,
    "÷" to Strings.inputDivide,
  )

/**
 * A calculator keypad typing an amount into [state], for a screen that shows the amount itself in
 * place of a text field. Each row of keys ends in one of [AmountField]'s operator keys. It takes
 * focus while shown, so a hardware keyboard types into it too.
 */
@Composable
fun AmountKeypad(
  state: TextFieldState,
  onDone: () -> Unit,
  modifier: Modifier = Modifier,
  canFinish: Boolean = true,
) {
  val operators = operatorKeys()
  val haptics = LocalHapticFeedback.current
  val focusRequester = remember { FocusRequester() }
  LaunchedEffect(focusRequester) { focusRequester.requestFocus() }

  Column(
    // Previewed, so Enter means Done even while one of the keys has focus from being clicked
    modifier =
      modifier
        .onPreviewKeyEvent { event ->
          when (val input = event.toKeypadInput()) {
            null -> return@onPreviewKeyEvent false
            is Append -> state.edit { append(input.text) }
            Backspace -> state.edit { if (length > 0) replace(length - 1, length, "") }
            Done -> if (canFinish) onDone()
          }
          true
        }
        .focusRequester(focusRequester)
        .focusable(),
    verticalArrangement = Arrangement.spacedBy(AmountFieldDS.keypadSpacing),
  ) {
    DIGIT_ROWS.fastForEachIndexed { index, digits ->
      KeypadRow {
        digits.fastForEach { digit -> KeypadKey(digit, onClick = { state.edit { append(digit) } }) }
        OperatorKey(operators[index], state)
      }
    }

    KeypadRow {
      KeypadKey(DECIMAL_POINT, onClick = { state.edit { append(DECIMAL_POINT) } })
      KeypadKey(ZERO, onClick = { state.edit { append(ZERO) } })
      KeypadKey(
        text = BACKSPACE,
        description = Strings.inputBackspace,
        onClick = { state.edit { if (length > 0) replace(length - 1, length, "") } },
      )
      OperatorKey(operators[DIGIT_ROWS.size], state)
    }

    PrimaryTextButton(
      modifier = Modifier.fillMaxWidth().height(AmountFieldDS.keyHeight),
      text = Strings.inputDone,
      isEnabled = canFinish,
      onClick = {
        haptics.performHapticFeedback(Confirm)
        onDone()
      },
    )
  }
}

internal sealed interface KeypadInput {
  data class Append(val text: String) : KeypadInput

  data object Backspace : KeypadInput

  data object Done : KeypadInput
}

private fun KeyEvent.toKeypadInput(): KeypadInput? {
  val isShortcut = isCtrlPressed || isMetaPressed || isAltPressed
  if (type != KeyDown || isShortcut) return null
  return keypadInput(key, utf16CodePoint.toChar())
}

// A hardware key as the keypad key it stands for, with operators typed as the keypad's symbols and
// a comma as its decimal point
internal fun keypadInput(key: Key, char: Char): KeypadInput? =
  when {
    key == Backspace -> Backspace
    key == Enter || key == NumPadEnter -> Done
    char in '0'..'9' -> KeypadInput.Append(char.toString())
    char == '.' || char == ',' -> KeypadInput.Append(DECIMAL_POINT)
    else -> HARDWARE_OPERATORS[char]?.let(KeypadInput::Append)
  }

@Composable
private fun RowScope.OperatorKey(operator: Pair<String, String>, state: TextFieldState) {
  val (symbol, description) = operator
  KeypadKey(symbol, description = description, onClick = { state.edit { append(symbol) } })
}

@Composable
private inline fun KeypadRow(content: @Composable RowScope.() -> Unit) =
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(AmountFieldDS.keypadSpacing),
    content = content,
  )

@Composable
private fun RowScope.KeypadKey(text: String, onClick: () -> Unit, description: String? = null) {
  val haptics = LocalHapticFeedback.current
  NormalTextButton(
    modifier =
      Modifier.weight(1f).height(AmountFieldDS.keyHeight).semantics {
        if (description != null) contentDescription = description
      },
    text = text,
    fontSize = AmountFieldDS.keyFontSize,
    onClick = {
      haptics.performHapticFeedback(KeyboardTap)
      onClick()
    },
  )
}

@Preview
@Composable
private fun PreviewAmountField(
  @PreviewParameter(ColorParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    AmountField(
      modifier = Modifier.padding(8.dp),
      state = rememberTextFieldState(initialText = "120 + 35.50"),
      label = "Amount",
      arithmetic = true,
    )
  }

@Preview
@Composable
private fun PreviewAmountKeypad(
  @PreviewParameter(ColorParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    AmountKeypad(
      modifier = Modifier.padding(8.dp),
      state = rememberTextFieldState(initialText = "42.10"),
      onDone = {},
    )
  }

private object AmountFieldDS {
  val spacing = 6.dp
  val keySpacing = 8.dp
  val keyFontSize = textSizes.extraLarge
  val keypadSpacing = 6.dp
  val keyHeight = 50.dp
}

private const val ZERO_PLACEHOLDER = "0.00"
private const val DECIMAL_POINT = "."
private const val ZERO = "0"
private const val BACKSPACE = "⌫"
private val HARDWARE_OPERATORS = mapOf('+' to "+", '-' to "−", '*' to "×", '/' to "÷")
private val DIGIT_ROWS = listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"))
