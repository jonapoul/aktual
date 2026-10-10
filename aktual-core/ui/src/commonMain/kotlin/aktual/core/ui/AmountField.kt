package aktual.core.ui

import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(AmountFieldDS.keySpacing),
  ) {
    operatorKeys().fastForEach { (symbol, description) ->
      NormalTextButton(
        modifier = Modifier.weight(1f).semantics { contentDescription = description },
        text = symbol,
        fontSize = AmountFieldDS.keyFontSize,
        onClick = { onKey(symbol) },
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
 * place of a text field. Each row of keys ends in one of [AmountField]'s operator keys.
 */
@Composable
fun AmountKeypad(
  state: TextFieldState,
  onDone: () -> Unit,
  modifier: Modifier = Modifier,
  canFinish: Boolean = true,
) {
  val operators = operatorKeys()
  Column(
    modifier = modifier,
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
      onClick = onDone,
    )
  }
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
private fun RowScope.KeypadKey(text: String, onClick: () -> Unit, description: String? = null) =
  NormalTextButton(
    modifier =
      Modifier.weight(1f).height(AmountFieldDS.keyHeight).semantics {
        if (description != null) contentDescription = description
      },
    text = text,
    fontSize = AmountFieldDS.keyFontSize,
    onClick = onClick,
  )

@Preview
@Composable
private fun PreviewAmountField(
  @PreviewParameter(ColoredParameters::class) colors: Colors,
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
  @PreviewParameter(ColoredParameters::class) colors: Colors,
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
  val keyFontSize = 20.sp
  val keypadSpacing = 6.dp
  val keyHeight = 50.dp
}

private const val ZERO_PLACEHOLDER = "0.00"
private const val DECIMAL_POINT = "."
private const val ZERO = "0"
private const val BACKSPACE = "⌫"
private val DIGIT_ROWS = listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"))
