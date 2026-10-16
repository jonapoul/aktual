package aktual.budget.formula.domain

import kotlin.math.pow

// Evaluates upstream Actual's formulas the way HyperFormula does, as if the formula were the only
// cell in a sheet. [variables] are HyperFormula's named expressions, looked up case-insensitively
class FormulaEvaluator internal constructor(private val functions: Map<String, FormulaFunction>) {
  constructor() : this(CORE_FUNCTIONS)

  suspend fun evaluate(
    formula: String,
    variables: Map<String, FormulaValue> = emptyMap(),
  ): FormulaResult {
    if (!formula.startsWith("=")) {
      return FormulaResult(ErrorValue(Generic, MISSING_EQUALS_MESSAGE), emptyList())
    }

    val root =
      try {
        FormulaParser.parse(formula)
      } catch (e: FormulaParseException) {
        return FormulaResult(ErrorValue(Generic, "$PARSE_ERROR_MESSAGE ${e.message}"), emptyList())
      }

    val scope = EvaluationScope(functions, variables.mapKeys { it.key.lowercase() })
    val value =
      when (val result = scope.evaluate(root).toScalar()) {
        is NumberValue -> NumberValue(roundForOutput(result.value))
        else -> result
      }
    return FormulaResult(value, scope.warnings.toList())
  }

  private companion object {
    const val MISSING_EQUALS_MESSAGE = "Formula must start with ="
    const val PARSE_ERROR_MESSAGE = "Parsing error."
  }
}

internal class EvaluationScope(
  private val functions: Map<String, FormulaFunction>,
  private val variables: Map<String, FormulaValue>,
) {
  val warnings = mutableListOf<FormulaWarning>()

  fun warn(warning: FormulaWarning) {
    warnings += warning
  }

  suspend fun evaluate(node: FormulaNode): FormulaValue {
    val value = evaluateWithoutPostProcessing(node)
    if (value !is NumberValue) return value
    return when {
      !value.value.isFinite() -> ErrorValue(Num, NAN_MESSAGE)
      // Turns -0.0 into 0.0
      value.value == 0.0 -> NumberValue(0.0)
      else -> value
    }
  }

  private suspend fun evaluateWithoutPostProcessing(node: FormulaNode): FormulaValue =
    when (node) {
      is NumberNode -> NumberValue(node.value)
      is TextNode -> TextValue(node.value)
      is ErrorNode -> ErrorValue(node.type)
      EmptyArgNode -> EmptyValue
      is CellReferenceNode -> if (node.isFormulaCell) ErrorValue(Cycle) else EmptyValue
      is NamedNode ->
        variables[node.name.lowercase()]
          ?: ErrorValue(Name, "Named expression ${node.name} not recognized.")
      is CallNode -> call(node)
      is UnaryNode -> unary(node.op, evaluate(node.operand).toScalar())
      is BinaryNode ->
        binary(node.op, evaluate(node.left).toScalar(), evaluate(node.right).toScalar())
      is ArrayNode -> array(node)
    }

  private suspend fun call(node: CallNode): FormulaValue {
    val function = functions[node.name]
    if (function != null) return function.call(this, node.args)
    val message =
      if (node.name in UPSTREAM_FUNCTION_NAMES) "Function ${node.name} isn't supported yet."
      else "Function name ${node.name} not recognized."
    return ErrorValue(Name, message)
  }

  private fun unary(op: UnaryOp, value: FormulaValue): FormulaValue {
    if (value is ErrorValue) return value
    return when (op) {
      Plus -> value
      Minus -> coerceToNumber(value).mapNumber { -it }
      Percent -> coerceToNumber(value).mapNumber { it / 100 }
    }
  }

  private fun binary(op: BinaryOp, left: FormulaValue, right: FormulaValue): FormulaValue {
    if (left is ErrorValue) return left
    if (right is ErrorValue) return right
    return when (op) {
      Equal -> BooleanValue(compareValues(left, right) == 0)
      NotEqual -> BooleanValue(compareValues(left, right) != 0)
      Greater -> BooleanValue(compareValues(left, right) > 0)
      Less -> BooleanValue(compareValues(left, right) < 0)
      GreaterOrEqual -> BooleanValue(compareValues(left, right) >= 0)
      LessOrEqual -> BooleanValue(compareValues(left, right) <= 0)
      Concat -> concat(left, right)
      Add,
      Subtract,
      Multiply,
      Divide,
      Power -> arithmetic(op, left, right)
    }
  }

  private fun concat(left: FormulaValue, right: FormulaValue): FormulaValue {
    val leftText = coerceToText(left) as TextValue
    val rightText = coerceToText(right) as TextValue
    return TextValue(leftText.value + rightText.value)
  }

  private fun arithmetic(op: BinaryOp, left: FormulaValue, right: FormulaValue): FormulaValue {
    val l = coerceToNumber(left)
    if (l !is NumberValue) return l
    val r = coerceToNumber(right)
    if (r !is NumberValue) return r
    val result =
      when (op) {
        Add -> addWithEpsilon(l.value, r.value)
        Subtract -> subtractWithEpsilon(l.value, r.value)
        Multiply -> l.value * r.value
        Divide -> if (r.value == 0.0) return ErrorValue(DivByZero) else l.value / r.value
        Power -> l.value.pow(r.value)
        else -> error("Not arithmetic: $op")
      }
    return NumberValue(result)
  }

  private suspend fun array(node: ArrayNode): FormulaValue {
    val result = mutableListOf<List<FormulaValue>>()
    for (rowNodes in node.rows) {
      var row: List<MutableList<FormulaValue>>? = null
      for (element in rowNodes) {
        val block =
          when (val value = evaluate(element)) {
            is ArrayValue -> value.rows
            else -> listOf(listOf(value))
          }
        if (row == null) {
          row = block.map { it.toMutableList() }
        } else if (row.size == block.size) {
          row.forEachIndexed { i, cells -> cells += block[i] }
        } else {
          return ErrorValue(Ref, SIZE_MISMATCH_MESSAGE)
        }
      }
      val rows = row.orEmpty()
      if (result.isNotEmpty() && result.first().size != rows.first().size) {
        return ErrorValue(Ref, SIZE_MISMATCH_MESSAGE)
      }
      result += rows
    }
    return ArrayValue(result)
  }

  private inline fun FormulaValue.mapNumber(transform: (Double) -> Double): FormulaValue =
    if (this is NumberValue) NumberValue(transform(value)) else this

  private companion object {
    const val NAN_MESSAGE = "NaN or infinite value encountered."
    const val SIZE_MISMATCH_MESSAGE = "Array dimensions mismatched."
  }
}
