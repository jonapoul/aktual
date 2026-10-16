package aktual.budget.formula.domain

// Mirrors the cell values HyperFormula produces, so formulas evaluate the same as upstream
sealed interface FormulaValue

data class NumberValue(val value: Double) : FormulaValue

data class TextValue(val value: String) : FormulaValue

data class BooleanValue(val value: Boolean) : FormulaValue

data object EmptyValue : FormulaValue

data class ErrorValue(val type: FormulaErrorType, val message: String = "") : FormulaValue

data class ArrayValue(val rows: List<List<FormulaValue>>) : FormulaValue {
  init {
    require(rows.isNotEmpty() && rows.first().isNotEmpty()) { "Empty array" }
    require(rows.all { it.size == rows.first().size }) { "Ragged array $rows" }
  }

  val topLeft: FormulaValue
    get() = rows.first().first()
}

enum class FormulaErrorType(val literal: String) {
  Cycle("#CYCLE!"),
  DivByZero("#DIV/0!"),
  Generic("#ERROR!"),
  Name("#NAME?"),
  NotAvailable("#N/A"),
  Num("#NUM!"),
  Ref("#REF!"),
  Spill("#SPILL!"),
  Value("#VALUE!"),
}

// Something the formula did that isn't an error but may be a mistake, e.g. naming a missing query
interface FormulaWarning

data class FormulaResult(val value: FormulaValue, val warnings: List<FormulaWarning>)
