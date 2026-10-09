package aktual.core.ui

import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
  val keys =
    listOf(
      "+" to Strings.inputAdd,
      "−" to Strings.inputSubtract,
      "×" to Strings.inputMultiply,
      "÷" to Strings.inputDivide,
    )

  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(AmountFieldDS.keySpacing),
  ) {
    for ((symbol, description) in keys) {
      NormalTextButton(
        modifier = Modifier.weight(1f).semantics { contentDescription = description },
        text = symbol,
        fontSize = AmountFieldDS.keyFontSize,
        onClick = { onKey(symbol) },
      )
    }
  }
}

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

private object AmountFieldDS {
  val spacing = 6.dp
  val keySpacing = 8.dp
  val keyFontSize = 20.sp
}

private const val ZERO_PLACEHOLDER = "0.00"
