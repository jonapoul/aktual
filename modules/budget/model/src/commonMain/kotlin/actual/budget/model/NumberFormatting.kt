package actual.budget.model

enum class NumberFormat(
  val value: String,
  val thousandsSeparator: Char,
  val decimalSeparator: Char,
) {
  // e.g. 1,000.33
  CommaDot(value = "comma-dot", thousandsSeparator = ',', decimalSeparator = '.'),

  // e.g. 1.000,33
  DotComma(value = "dot-comma", thousandsSeparator = '.', decimalSeparator = ','),

  // e.g. 1 000,33
  SpaceComma(value = "space-comma", thousandsSeparator = '\u00A0', decimalSeparator = ','),

  // e.g. 1'000.33
  ApostropheDot(value = "apostrophe-dot", thousandsSeparator = '’', decimalSeparator = '.'),

  // e.g. 1,00,000.33
  CommaDotIn(value = "comma-dot-in", thousandsSeparator = ',', decimalSeparator = '.'),
  ;

  companion object {
    fun from(value: String): NumberFormat? = entries.firstOrNull { it.value == value }
  }
}

data class NumberFormatConfig(
  val format: NumberFormat,
  val hideFraction: Boolean,
)
