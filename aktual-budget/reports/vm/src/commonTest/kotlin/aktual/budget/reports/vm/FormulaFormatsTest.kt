package aktual.budget.reports.vm

import aktual.budget.formula.domain.FormulaFormat
import aktual.budget.formula.domain.NARROW_NO_BREAK_SPACE
import aktual.budget.model.CurrencyConfig
import aktual.budget.model.NumberFormatConfig
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class FormulaFormatsTest {
  @Test
  fun `No currency formats as dollars`() {
    val currency = CurrencyConfig(None, position = AfterAmount, includeSpace = true)
    assertThat(formulaFormat(NumberFormatConfig(DotComma, hideFraction = false), currency))
      .isEqualTo(
        FormulaFormat(
          decimalPlaces = 2,
          thousandsSeparator = ".",
          decimalSeparator = ",",
          currencySymbol = "$",
          currencyDecimalPlaces = 2,
          symbolAfterAmount = false,
          spaceBetweenAmountAndSymbol = false,
        ),
      )
  }

  @Test
  fun `Currency preferences are used`() {
    val currency = CurrencyConfig(Euro, position = AfterAmount, includeSpace = true)
    assertThat(formulaFormat(NumberFormatConfig(SpaceComma, hideFraction = true), currency))
      .isEqualTo(
        FormulaFormat(
          decimalPlaces = 0,
          thousandsSeparator = NARROW_NO_BREAK_SPACE,
          decimalSeparator = ",",
          currencySymbol = "€",
          currencyDecimalPlaces = 2,
          symbolAfterAmount = true,
          spaceBetweenAmountAndSymbol = true,
        ),
      )
  }
}
