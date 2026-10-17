package aktual.budget.reports.vm

import aktual.budget.formula.FormulaFormat
import aktual.budget.formula.NARROW_NO_BREAK_SPACE
import aktual.budget.model.Currency
import aktual.budget.model.CurrencyConfig
import aktual.budget.model.NumberFormatConfig

// packages/loot-core/src/server/formulas/customFunctionsPreferences.ts: the user's format for the
// formatting functions. Without a currency, upstream formats as US dollars
internal fun formulaFormat(
  numberFormat: NumberFormatConfig,
  currency: CurrencyConfig,
): FormulaFormat {
  val hasCurrency = currency.currency != None
  val formatCurrency = if (hasCurrency) currency.currency else Currency.UsDollar
  val format = numberFormat.format
  return FormulaFormat(
    decimalPlaces = if (numberFormat.hideFraction) 0 else formatCurrency.decimalPlaces,
    thousandsSeparator =
      if (format == SpaceComma) NARROW_NO_BREAK_SPACE else format.thousandsSeparator,
    decimalSeparator = format.decimalSeparator,
    currencySymbol = formatCurrency.symbol,
    currencyDecimalPlaces = formatCurrency.decimalPlaces,
    symbolAfterAmount = hasCurrency && currency.position == AfterAmount,
    spaceBetweenAmountAndSymbol = hasCurrency && currency.includeSpace,
  )
}
