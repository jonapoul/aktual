package aktual.budget.formula

// Port of HyperFormula's BooleanPlugin
internal val LOGICAL_FUNCTIONS: Map<String, FormulaFunction> =
  mapOf(
    "TRUE" to formulaFunction { BooleanValue(true) },
    "FALSE" to formulaFunction { BooleanValue(false) },
    "IF" to
      formulaFunction(
        Param(Logical),
        Param(Scalar),
        Param(Scalar, default = BooleanValue(false)),
      ) { args ->
        if (args.boolean(0)) args.value(1) else args.value(2)
      },
    "IFS" to
      formulaFunction(Param(Logical), Param(Scalar), repeatLastArgs = 2) { args ->
        (0 until args.size step 2).firstOrNull { args.boolean(it) }?.let { args.value(it + 1) }
          ?: ErrorValue(NotAvailable, NoConditionMet)
      },
    "AND" to
      formulaFunction(Param(Logical), repeatLastArgs = 1, expandRanges = true) { args ->
        BooleanValue(args.booleans().all { it })
      },
    "OR" to
      formulaFunction(Param(Logical), repeatLastArgs = 1, expandRanges = true) { args ->
        BooleanValue(args.booleans().any { it })
      },
    "XOR" to
      formulaFunction(Param(Logical), repeatLastArgs = 1, expandRanges = true) { args ->
        BooleanValue(args.booleans().count { it } % 2 == 1)
      },
    "NOT" to formulaFunction(Param(Logical)) { args -> BooleanValue(!args.boolean(0)) },
    "SWITCH" to
      formulaFunction(Param(NoError), Param(Scalar), Param(Scalar), repeatLastArgs = 1) { args ->
        switch(args)
      },
    "IFERROR" to
      formulaFunction(Param(Scalar), Param(Scalar)) { args ->
        if (args.value(0) is ErrorValue) args.value(1) else args.value(0)
      },
    "IFNA" to
      formulaFunction(Param(Scalar), Param(Scalar)) { args ->
        val value = args.value(0)
        if (value is ErrorValue && value.type == NotAvailable) args.value(1) else value
      },
    "CHOOSE" to
      formulaFunction(Param(Integral, minValue = 1.0), Param(Scalar), repeatLastArgs = 1) { args ->
        val selector = args.number(0)
        if (selector > args.size - 1) {
          ErrorValue(Num, SelectorTooLarge)
        } else {
          args.value(selector.toInt())
        }
      },
  )

private fun switch(args: FunctionArgs): FormulaValue {
  val selector = args.value(0)
  val cases = (1 until args.size).map(args::value)
  var i = 0
  while (i + 1 < cases.size) {
    val case = cases[i]
    if (case !is ErrorValue && compareValues(selector, case) == 0) return cases[i + 1]
    i += 2
  }
  return if (i < cases.size) cases[i] else ErrorValue(NotAvailable, NoDefault)
}
