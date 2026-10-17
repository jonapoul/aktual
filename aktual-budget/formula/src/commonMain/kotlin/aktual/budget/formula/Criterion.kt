package aktual.budget.formula

// Port of HyperFormula's CriterionBuilder, which reads the criteria of COUNTIF and SUMIF like ">10"
// or "a*"
internal fun interface Criterion {
  fun matches(value: FormulaValue): Boolean
}

// Null when the criterion isn't valid
internal fun criterion(value: FormulaValue): Criterion? =
  when (value) {
    is NumberValue -> numberCriterion(Equal, value.value)
    is BooleanValue -> booleanCriterion(Equal, value.value)
    is TextValue -> textCriterion(value.value)
    EmptyValue,
    is ErrorValue,
    is ArrayValue -> null
  }

private enum class Operator {
  Greater,
  GreaterOrEqual,
  Less,
  LessOrEqual,
  NotEqual,
  Equal,
}

private fun textCriterion(text: String): Criterion? {
  // Not anchored, like upstream, so anything before the operator is ignored
  val match = OPERATOR.find(text)
  val op: Operator = if (match == null) Equal else OPERATORS[match.groupValues[1]] ?: return null
  val operand = match?.groupValues?.get(2) ?: text
  if (operand.isEmpty()) return emptyCriterion(op)
  val number = textToNumber(operand)
  return when {
    number != null -> numberCriterion(op, number)
    op != Equal && op != NotEqual -> null
    operand.lowercase() == "true" -> booleanCriterion(op, true)
    operand.lowercase() == "false" -> booleanCriterion(op, false)
    else -> patternCriterion(op, operand)
  }
}

private fun numberCriterion(op: Operator, number: Double) = Criterion { value ->
  when (op) {
    Equal -> value.criterionNumber()?.let { floatCompare(it, number) == 0 } == true
    NotEqual -> value.criterionNumber()?.let { floatCompare(it, number) != 0 } != false
    Greater -> value is NumberValue && floatCompare(value.value, number) > 0
    GreaterOrEqual -> value is NumberValue && floatCompare(value.value, number) >= 0
    Less -> value is NumberValue && floatCompare(value.value, number) < 0
    LessOrEqual -> value is NumberValue && floatCompare(value.value, number) <= 0
  }
}

// Text that reads as a number matches it too, but empty text doesn't
private fun FormulaValue.criterionNumber(): Double? =
  when (this) {
    is NumberValue -> value
    is TextValue -> if (value.isEmpty()) null else textToNumber(value)
    is BooleanValue,
    EmptyValue,
    is ErrorValue,
    is ArrayValue -> null
  }

// Only reached with Equal or NotEqual
private fun booleanCriterion(op: Operator, boolean: Boolean) = Criterion { value ->
  val equal = value is BooleanValue && value.value == boolean
  if (op == Equal) equal else !equal
}

private fun emptyCriterion(op: Operator) = Criterion { value ->
  when (op) {
    Equal -> value == EmptyValue
    NotEqual -> value != EmptyValue
    Greater,
    GreaterOrEqual,
    Less,
    LessOrEqual -> false
  }
}

// Only reached with Equal or NotEqual
private fun patternCriterion(op: Operator, pattern: String): Criterion {
  val regex = Regex(wildcardRegex(normalize(pattern)))
  return Criterion { value ->
    val equal = value is TextValue && regex.matches(normalize(value.value))
    if (op == Equal) equal else !equal
  }
}

private fun normalize(text: String): String = stripAccents(text.lowercase())

private val OPERATOR = Regex("([<>=]+)(.*)")

private val OPERATORS =
  mapOf(
    ">" to Operator.Greater,
    ">=" to Operator.GreaterOrEqual,
    "<" to Operator.Less,
    "<=" to Operator.LessOrEqual,
    "<>" to Operator.NotEqual,
    "=" to Operator.Equal,
  )
