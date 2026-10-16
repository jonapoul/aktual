package aktual.budget.formula.domain

import aktual.core.Calendar
import kotlin.math.pow

// Evaluates upstream Actual's formulas the way HyperFormula does, as if the formula were the only
// cell in a sheet. [variables] are HyperFormula's named expressions, looked up case-insensitively
class FormulaEvaluator internal constructor(private val functions: Map<String, FormulaFunction>) {
  constructor(calendar: Calendar) : this(builtInFunctions(calendar))

  suspend fun evaluate(
    formula: String,
    variables: Map<String, FormulaValue> = emptyMap(),
  ): FormulaResult {
    if (!formula.startsWith("=")) {
      return FormulaResult(ErrorValue(Generic, MissingEquals), emptyList())
    }

    val root =
      try {
        FormulaParser.parse(formula)
      } catch (e: FormulaParseException) {
        return FormulaResult(ErrorValue(Generic, e.reason, e.subject), emptyList())
      }

    val scope = EvaluationScope(functions, variables.mapKeys { it.key.lowercase() })
    val value =
      when (val result = scope.evaluate(root).toScalar()) {
        is NumberValue -> NumberValue(roundForOutput(result.value))
        else -> result
      }
    return FormulaResult(value, scope.warnings.toList())
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

  suspend fun evaluate(node: FormulaNode): FormulaValue =
    when (val value = evaluateWithoutPostProcessing(node)) {
      is NumberValue ->
        when {
          !value.value.isFinite() -> ErrorValue(Num, NotFinite)
          // Turns -0.0 into 0.0
          value.value == 0.0 -> NumberValue(0.0)
          else -> value
        }
      is ArrayValue -> if (value.rows.size == 1 && value.rows[0].size == 1) value.topLeft else value
      else -> value
    }

  private suspend fun evaluateWithoutPostProcessing(node: FormulaNode): FormulaValue =
    when (node) {
      is NumberNode -> NumberValue(node.value)
      is TextNode -> TextValue(node.value)
      is ErrorNode -> ErrorValue(node.type)
      EmptyArgNode -> EmptyValue
      is CellReferenceNode -> if (node.isFormulaCell) ErrorValue(Cycle) else EmptyValue
      is NamedNode -> variables[node.name.lowercase()] ?: ErrorValue(Name, UnknownName, node.name)
      is CallNode -> call(node)
      is UnaryNode -> unary(node.op, evaluate(node.operand).toScalar())
      is BinaryNode ->
        binary(node.op, evaluate(node.left).toScalar(), evaluate(node.right).toScalar())
      is ArrayNode -> array(node)
    }

  private suspend fun call(node: CallNode): FormulaValue {
    val function = functions[node.name]
    if (function != null) return function.call(this, node.args)
    val isUpstream = node.name in UPSTREAM_FUNCTION_NAMES
    return ErrorValue(Name, if (isUpstream) UnsupportedFunction else UnknownFunction, node.name)
  }

  private fun unary(op: UnaryOp, value: FormulaValue): FormulaValue {
    if (value is ErrorValue) return value
    return when (op) {
      Plus -> value
      Minus -> coerceToNumber(value).mapNumber { -it }
      Percent -> coerceToNumber(value).mapNumber { it / PERCENT }
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
    if (op == Divide && r.value == 0.0) return ErrorValue(DivByZero)
    val result =
      when (op) {
        Add -> addWithEpsilon(l.value, r.value)
        Subtract -> subtractWithEpsilon(l.value, r.value)
        Multiply -> l.value * r.value
        Divide -> l.value / r.value
        Power -> l.value.pow(r.value)
        Equal,
        NotEqual,
        Greater,
        Less,
        GreaterOrEqual,
        LessOrEqual,
        Concat -> error("Not arithmetic: $op")
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
          return ErrorValue(Ref, ArraySizeMismatch)
        }
      }
      val rows = row.orEmpty()
      if (result.isNotEmpty() && result.first().size != rows.first().size) {
        return ErrorValue(Ref, ArraySizeMismatch)
      }
      result += rows
    }
    return ArrayValue(result)
  }

  private inline fun FormulaValue.mapNumber(transform: (Double) -> Double): FormulaValue =
    if (this is NumberValue) NumberValue(transform(value)) else this

  private companion object {
    const val PERCENT = 100
  }
}
