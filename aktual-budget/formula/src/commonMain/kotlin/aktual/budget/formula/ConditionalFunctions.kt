package aktual.budget.formula

// Port of the matching functions in HyperFormula's ConditionalAggregationPlugin. Each condition is
// a range and a criterion, and the values in the same positions are kept where every criterion
// matches
internal val CONDITIONAL_FUNCTIONS: Map<String, FormulaFunction> =
  mapOf(
    "SUMIF" to
      formulaFunction(Param(Range), Param(NoError), Param(Range, optional = true)) { args ->
        args.matching(args[2] as? ArrayValue ?: args.array(0), conditions = 0..0, ::sum)
      },
    "SUMIFS" to
      formulaFunction(Param(Range), Param(Range), Param(NoError), repeatLastArgs = 2) { args ->
        args.matching(args.array(0), conditions = 1 until args.size step 2, ::sum)
      },
    "COUNTIF" to
      formulaFunction(Param(Range), Param(NoError)) { args ->
        args.matching(args.array(0), conditions = 0..0, ::count)
      },
    "COUNTIFS" to
      formulaFunction(Param(Range), Param(NoError), repeatLastArgs = 2) { args ->
        args.matching(args.array(0), conditions = 0 until args.size step 2, ::count)
      },
  )

// [conditions] are the indexes of each condition's range, with its criterion after it
private fun FunctionArgs.matching(
  values: ArrayValue,
  conditions: IntProgression,
  reduce: (List<FormulaValue>) -> FormulaValue,
): FormulaValue {
  val criteria = conditions.map {
    criterion(value(it + 1)) ?: return ErrorValue(Value, BadCriterion)
  }
  val ranges = conditions.map(::array)
  if (ranges.any { !it.hasSizeOf(values) }) return ErrorValue(Value, ArraySizeMismatch)
  val cells = ranges.map { it.rows.flatten() }
  val matches =
    values.rows.flatten().filterIndexed { i, _ ->
      criteria.indices.all { criteria[it].matches(cells[it][i]) }
    }
  return reduce(matches)
}

// Errors are only kept from the matched values, and anything else that isn't a number is skipped
private fun sum(values: List<FormulaValue>): FormulaValue {
  var total = 0.0
  for (value in values) {
    if (value is ErrorValue) return value
    if (value is NumberValue) total = addWithEpsilon(total, value.value)
  }
  return NumberValue(total)
}

private fun count(values: List<FormulaValue>): FormulaValue = NumberValue(values.size.toDouble())
