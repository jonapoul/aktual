package aktual.budget.formula

// The user's number format and currency, used by FORMATNUMBER and FORMATCURRENCY for the arguments
// left out. The defaults are upstream's, for when it can't load the user's preferences
data class FormulaFormat(
  val decimalPlaces: Int = 2,
  val thousandsSeparator: String = ",",
  val decimalSeparator: String = ".",
  val currencySymbol: String = "$",
  val currencyDecimalPlaces: Int = 2,
  val symbolAfterAmount: Boolean = false,
  val spaceBetweenAmountAndSymbol: Boolean = false,
)

// Upstream puts it between amounts and currency symbols, and between thousands in the space-comma
// number format
const val NARROW_NO_BREAK_SPACE = "\u202F"
