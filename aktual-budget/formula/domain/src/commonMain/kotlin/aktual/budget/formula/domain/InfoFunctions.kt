package aktual.budget.formula.domain

// Port of the matching functions in HyperFormula's InformationPlugin
internal val INFO_FUNCTIONS: Map<String, FormulaFunction> =
  mapOf(
    "ISERROR" to
      formulaFunction(Param(Scalar)) { args -> BooleanValue(args.value(0) is ErrorValue) },
    "ISBLANK" to
      formulaFunction(Param(Scalar)) { args -> BooleanValue(args.value(0) == EmptyValue) },
    "ISNUMBER" to
      formulaFunction(Param(Scalar)) { args -> BooleanValue(args.value(0) is NumberValue) },
    "ISTEXT" to formulaFunction(Param(Scalar)) { args -> BooleanValue(args.value(0) is TextValue) },
  )
