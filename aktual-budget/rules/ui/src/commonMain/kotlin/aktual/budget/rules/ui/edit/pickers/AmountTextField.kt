package aktual.budget.rules.ui.edit.pickers

import aktual.budget.model.Amount
import aktual.budget.model.Currency
import aktual.budget.model.CurrencyConfig
import aktual.budget.model.CurrencySymbolPosition
import aktual.budget.model.CurrencySymbolPosition.AfterAmount
import aktual.budget.model.CurrencySymbolPosition.BeforeAmount
import aktual.budget.model.NumberFormat
import aktual.budget.model.NumberFormatConfig
import aktual.core.icons.Add
import aktual.core.icons.AktualIcons
import aktual.core.icons.Subtract
import aktual.core.icons.material.Clear
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.theme.DarkColors
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTypography
import aktual.core.ui.BareIconButton
import aktual.core.ui.IconButtonColorProvider
import aktual.core.ui.LocalCurrencyConfig
import aktual.core.ui.LocalNumberFormatConfig
import aktual.core.ui.LocalPrivacyEnabled
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.bareIconButton
import aktual.core.ui.formattedString
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

@Composable
internal fun AmountTextField(
  value: JsonElement,
  isEnabled: Boolean,
  onValueChange: (JsonElement) -> Unit,
  modifier: Modifier = Modifier,
  currencyConfig: CurrencyConfig = LocalCurrencyConfig.current,
  numberFormatConfig: NumberFormatConfig = LocalNumberFormatConfig.current,
  isPrivacyEnabled: Boolean = LocalPrivacyEnabled.current,
) {
  require(value is JsonNull || value is JsonPrimitive) { "Need primitive or null, got $value" }

  val rememberOnValueChange by rememberUpdatedState(onValueChange)

  val textStyle =
    LocalTextStyle.current.copy(
      textAlign =
        when (currencyConfig.position) {
          BeforeAmount -> TextAlign.Start
          AfterAmount -> TextAlign.End
        }
    )

  var lastEmittedValue by remember { mutableStateOf(value) }
  val initialText =
    when (value) {
      is JsonNull -> ""
      is JsonPrimitive -> value.content.removePrefix("-").removePrefix("+")
    }
  val textState = rememberTextFieldState(initialText = initialText)
  var isPositive by remember { mutableStateOf(value.isPositive()) }

  // Sync text field from external value changes (field type change, condition reset)
  LaunchedEffect(value) {
    if (value != lastEmittedValue) {
      val newText =
        when (value) {
          is JsonNull -> ""
          is JsonPrimitive -> value.content.removePrefix("-").removePrefix("+")
        }
      textState.edit { replace(0, length, newText) }
      isPositive = value.isPositive()
      lastEmittedValue = value
    }
  }

  // Feed user input (text + sign) back to parent
  LaunchedEffect(textState) {
    snapshotFlow { textState.text.toString() to isPositive }
      .distinctUntilChanged()
      .collect { (text, positive) ->
        val amount = text.toLongOrNull()
        val json = if (amount == null) JsonNull else JsonPrimitive(amount * if (positive) 1 else -1)
        lastEmittedValue = json
        rememberOnValueChange(json)
      }
  }

  AktualTextField(
    modifier = modifier.fillMaxWidth(),
    state = textState,
    placeholderText =
      Amount.Zero.formattedString(currencyConfig = currencyConfig.copy(currency = Currency.None)),
    isEnabled = isEnabled,
    leadingIcon = {
      LeadingContent(
        isEnabled = isEnabled,
        isPositive = isPositive,
        onToggleSign = { isPositive = it },
      )
    },
    trailingIcon = {
      TrailingContent(
        isEnabled = isEnabled,
        canClear = value != JsonNull,
        onClear = { rememberOnValueChange(JsonNull) },
      )
    },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
    singleLine = true,
    textStyle = textStyle,
    outputTransformation =
      remember(numberFormatConfig, currencyConfig, isPrivacyEnabled) {
        NumberOutputTransformation(numberFormatConfig, currencyConfig, isPrivacyEnabled)
      },
  )
}

private fun JsonPrimitive.isPositive(): Boolean {
  if (this is JsonNull) return true
  val amount = content.amountOrNull() ?: return true
  return amount >= Amount.Zero
}

@Composable
private fun LeadingContent(
  isEnabled: Boolean,
  isPositive: Boolean,
  onToggleSign: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  config: CurrencyConfig = LocalCurrencyConfig.current,
) {
  Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
    BareIconButton(
      enabled = isEnabled,
      imageVector = if (isPositive) AktualIcons.Add else AktualIcons.Subtract,
      contentDescription =
        if (isPositive) Strings.editRuleAmountSignPositive else Strings.editRuleAmountSignNegative,
      onClick = { onToggleSign(!isPositive) },
      colors = if (isPositive) PositiveColors else NegativeColors,
    )

    if (config.currency != Currency.None && config.position == BeforeAmount) {
      Text(
        modifier = Modifier.minimumInteractiveComponentSize(),
        text = config.currency.symbol,
        style = AktualTypography.bodyMedium,
        color = colors.pageTextSubdued,
      )
    }
  }
}

private val NegativeColors = IconButtonColorProvider { theme, isPressed ->
  theme
    .bareIconButton(isPressed)
    .copy(contentColor = theme.numberNegative, disabledContentColor = theme.pageTextSubdued)
}

private val PositiveColors = IconButtonColorProvider { theme, isPressed ->
  theme
    .bareIconButton(isPressed)
    .copy(contentColor = theme.numberPositive, disabledContentColor = theme.pageTextSubdued)
}

@Composable
private fun TrailingContent(
  isEnabled: Boolean,
  canClear: Boolean,
  onClear: () -> Unit,
  modifier: Modifier = Modifier,
  config: CurrencyConfig = LocalCurrencyConfig.current,
) {
  Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
    if (config.currency != Currency.None && config.position == AfterAmount) {
      Text(
        modifier = Modifier.minimumInteractiveComponentSize(),
        text = config.currency.symbol,
        style = AktualTypography.bodyMedium,
        color = colors.pageTextSubdued,
      )
    }

    if (canClear) {
      BareIconButton(
        enabled = isEnabled,
        imageVector = MaterialIcons.Clear,
        contentDescription = Strings.editRuleAmountClear,
        onClick = onClear,
      )
    }
  }
}

private class NumberOutputTransformation(
  private val numberFormatConfig: NumberFormatConfig,
  private val currencyConfig: CurrencyConfig,
  private val isPrivacyEnabled: Boolean,
) : OutputTransformation {
  override fun TextFieldBuffer.transformOutput() {
    val rawText = toString()
    if (rawText.isEmpty()) return
    val amount = rawText.amountOrNull() ?: Amount.Zero
    val formatted =
      amount.toString(
        numberFormatConfig = numberFormatConfig,
        currencyConfig = currencyConfig.copy(currency = Currency.None),
        includeSign = false,
        isPrivacyEnabled = isPrivacyEnabled,
      )
    replace(0, length, formatted)
  }
}

private fun String.amountOrNull(): Amount? = toLongOrNull()?.let(::Amount)

@Preview
@Composable
private fun PreviewAmountTextField(
  @PreviewParameter(AmountTextFieldProvider::class) params: AmountTextFieldParams
) {
  PreviewWithColors(DarkColors) {
    with(params) {
      var element by remember { mutableStateOf(value) }
      AmountTextField(
        value = element,
        isEnabled = isEnabled,
        onValueChange = { element = it },
        currencyConfig = CurrencyConfig(currency, position, includeSpace),
        numberFormatConfig = NumberFormatConfig(numberFormat, hideFraction = false),
        isPrivacyEnabled = isPrivacyEnabled,
      )
    }
  }
}

private data class AmountTextFieldParams(
  val value: JsonElement,
  val isEnabled: Boolean = false,
  val currency: Currency = Currency.PoundSterling,
  val position: CurrencySymbolPosition = CurrencySymbolPosition.Default,
  val includeSpace: Boolean = true,
  val numberFormat: NumberFormat = NumberFormat.Default,
  val isPrivacyEnabled: Boolean = false,
)

@Suppress("MagicNumber")
private class AmountTextFieldProvider :
  CollectionPreviewParameterProvider<AmountTextFieldParams>(
    listOf(
      AmountTextFieldParams(value = JsonNull),
      AmountTextFieldParams(value = JsonPrimitive(5)),
      AmountTextFieldParams(value = JsonPrimitive(12345)),
      AmountTextFieldParams(value = JsonPrimitive(-12345)),
      AmountTextFieldParams(value = JsonPrimitive(12345), isEnabled = false),
      AmountTextFieldParams(value = JsonPrimitive(12345), position = AfterAmount),
      AmountTextFieldParams(value = JsonPrimitive(12345), includeSpace = false),
      AmountTextFieldParams(value = JsonPrimitive(12345), currency = Currency.SwedishKrona),
    )
  )
