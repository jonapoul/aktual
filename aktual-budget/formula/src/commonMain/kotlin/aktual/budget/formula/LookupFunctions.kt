package aktual.budget.formula

import kotlin.math.floor

// Port of the matching functions in HyperFormula's LookupPlugin, plus INDEX from its
// InformationPlugin
internal val LOOKUP_FUNCTIONS: Map<String, FormulaFunction> =
  mapOf(
    "INDEX" to
      formulaFunction(
        Param(Range),
        Param(Numeric),
        Param(Numeric, default = NumberValue(1.0)),
      ) { args ->
        val array = args.array(0)
        val row = args.number(1)
        val column = args.number(2)
        when {
          row < 1 || column < 1 -> ErrorValue(Value, LessThanOne)
          row > array.height || column > array.width -> ErrorValue(Num, ValueTooLarge)
          // Upstream falls back to the top left value when a position isn't a whole number
          row != floor(row) || column != floor(column) -> array.topLeft
          else -> array.rows[row.toInt() - 1][column.toInt() - 1]
        }
      },
    "MATCH" to
      formulaFunction(
        Param(NoError),
        Param(Range),
        Param(Numeric, default = NumberValue(1.0)),
      ) { args ->
        match(args.value(0), args.array(1), args.number(2))
      },
    "VLOOKUP" to lookup(),
    "HLOOKUP" to lookup(),
  )

// Upstream only looks up values in a cell range, which formulas can't have
private fun lookup(): FormulaFunction =
  formulaFunction(
    Param(NoError),
    Param(Range),
    Param(Numeric),
    Param(Logical, default = BooleanValue(true)),
  ) {
    ErrorValue(Value, WrongArgType)
  }

// Port of LookupPlugin.doMatch for values that aren't in a cell range: 0 finds an exact match, 1
// the largest value at most the key and -1 the smallest value at least the key. Neither needs the
// values to be sorted
private fun match(key: FormulaValue, array: ArrayValue, type: Double): FormulaValue {
  if (type != -1.0 && type != 0.0 && type != 1.0) return ErrorValue(Value, BadMode)
  if (array.height > 1 && array.width > 1) return ErrorValue(NotAvailable)
  val searchKey = normalized(if (key == EmptyValue) NumberValue(0.0) else key)
  val values = array.rows.flatten().map(::normalized)
  val index = if (type == 0.0) values.indexOf(searchKey) else bound(searchKey, values, type)
  return if (index < 0) ErrorValue(NotAvailable, ValueNotFound) else NumberValue(index + 1.0)
}

private fun normalized(value: FormulaValue): FormulaValue =
  if (value is TextValue) TextValue(stripAccents(value.value.lowercase())) else value

private fun bound(key: FormulaValue, values: List<FormulaValue>, direction: Double): Int {
  var best: FormulaValue = NumberValue(Double.NEGATIVE_INFINITY * direction)
  var bestIndex = -1
  for ((index, value) in values.withIndex()) {
    if (value == key) return index
    if (direction * lookupCompare(value, key) <= 0 && direction * lookupCompare(best, value) < 0) {
      best = value
      bestIndex = index
    }
  }
  return bestIndex
}

// Port of binarySearch.compare, which orders values by type before value
private fun lookupCompare(left: FormulaValue, right: FormulaValue): Int =
  when {
    left is NumberValue && right is NumberValue -> left.value.compareTo(right.value)
    left is TextValue && right is TextValue -> left.value.compareTo(right.value)
    left is BooleanValue && right is BooleanValue -> left.value.compareTo(right.value)
    left::class == right::class -> 0
    left == EmptyValue -> -1
    right == EmptyValue -> 1
    right is ErrorValue -> -1
    left is NumberValue -> -1
    left is TextValue -> if (right is NumberValue) 1 else -1
    else -> 1
  }
