package aktual.budget.formula

import kotlin.math.floor

internal fun interface FormulaFunction {
  suspend fun call(scope: EvaluationScope, args: List<FormulaNode>): FormulaValue
}

// HyperFormula's FunctionArgumentType
internal enum class ArgType {
  Numeric,
  Integral,
  Text,
  Logical,
  Scalar,
  NoError,
  Anything,
  Range,
}

internal data class Param(
  val type: ArgType,
  val optional: Boolean = false,
  val default: FormulaValue? = null,
  val minValue: Double? = null,
  val maxValue: Double? = null,
  val greaterThan: Double? = null,
  val lessThan: Double? = null,
)

// Coerced argument values, null where an optional argument without a default was left out
internal class FunctionArgs(private val values: List<FormulaValue?>) {
  val size: Int
    get() = values.size

  operator fun get(index: Int): FormulaValue? = values.getOrNull(index)

  fun value(index: Int): FormulaValue = requireNotNull(values[index])

  fun number(index: Int): Double = (value(index) as NumberValue).value

  fun text(index: Int): String = (value(index) as TextValue).value

  fun boolean(index: Int): Boolean = (value(index) as BooleanValue).value

  fun array(index: Int): ArrayValue = value(index) as ArrayValue

  // Skips the values left out because they couldn't be coerced
  fun texts(): List<String> = values.filterIsInstance<TextValue>().map { it.value }

  fun booleans(): List<Boolean> = values.filterIsInstance<BooleanValue>().map { it.value }
}

// Port of HyperFormula's FunctionPlugin.runFunction: evaluates every argument, checks the count,
// fills in defaults and coerces each value to its parameter's type before calling [body]
internal fun formulaFunction(
  vararg params: Param,
  repeatLastArgs: Int = 0,
  expandRanges: Boolean = false,
  body: suspend EvaluationScope.(FunctionArgs) -> FormulaValue,
): FormulaFunction = FormulaFunction { scope, args ->
  scope.runFunction(args, params.toList(), repeatLastArgs, expandRanges, body)
}

private suspend fun EvaluationScope.runFunction(
  args: List<FormulaNode>,
  params: List<Param>,
  repeatLastArgs: Int,
  expandRanges: Boolean,
  body: suspend EvaluationScope.(FunctionArgs) -> FormulaValue,
): FormulaValue {
  // Each value is paired with whether it came out of an expanded array, so can be skipped
  val values =
    if (expandRanges) {
      args.flatMap { arg ->
        val value = evaluate(arg)
        if (value is ArrayValue) value.rows.flatten().map { it to true } else listOf(value to false)
      }
    } else {
      args.map { evaluate(it) to false }
    }

  val metadata = params.toMutableList()
  if (repeatLastArgs > 0) {
    while (values.size > metadata.size) metadata += params.takeLast(repeatLastArgs)
  }
  if (values.size > metadata.size) return ErrorValue(NotAvailable, WrongArgCount)
  val missingAreOptional = metadata.drop(values.size).all { it.optional || it.default != null }
  if (!missingAreOptional) return ErrorValue(NotAvailable, WrongArgCount)

  val coerced = ArrayList<FormulaValue?>(metadata.size)
  for ((index, param) in metadata.withIndex()) {
    val argument = values.getOrNull(index)
    val value = argument?.first ?: param.default
    if (value == null) {
      coerced += null
      continue
    }
    val coercedValue = coerceToType(value, param)
    val ignorable = argument?.second == true
    if (coercedValue == null && !ignorable) return ErrorValue(Value, WrongArgType)
    if (coercedValue is ErrorValue && param.type != Scalar) return coercedValue
    coerced += coercedValue
  }
  return body(FunctionArgs(coerced))
}

private fun coerceToType(value: FormulaValue, param: Param): FormulaValue? {
  if (value is ArrayValue && (param.type == Range || param.type == Anything)) return value
  val scalar = value.toScalar()
  return when (param.type) {
    Numeric,
    Integral -> coerceToNumberParam(scalar, param)
    Text -> coerceToText(scalar)
    Logical -> coerceToBoolean(scalar)
    Scalar,
    NoError,
    Anything -> scalar
    Range -> scalar as? ErrorValue ?: ArrayValue(listOf(listOf(scalar)))
  }
}

private fun coerceToNumberParam(value: FormulaValue, param: Param): FormulaValue {
  val coerced = coerceToNumber(value)
  if (coerced !is NumberValue) return coerced
  val number = coerced.value
  return when {
    param.maxValue != null && number > param.maxValue -> ErrorValue(Num, ValueTooLarge)
    param.minValue != null && number < param.minValue -> ErrorValue(Num, ValueTooSmall)
    param.lessThan != null && number >= param.lessThan -> ErrorValue(Num, ValueTooLarge)
    param.greaterThan != null && number <= param.greaterThan -> ErrorValue(Num, ValueTooSmall)
    param.type == Integral && number != floor(number) -> ErrorValue(Num, IntegerExpected)
    else -> coerced
  }
}
