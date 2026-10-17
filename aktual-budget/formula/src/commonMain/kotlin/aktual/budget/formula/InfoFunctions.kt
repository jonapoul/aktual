package aktual.budget.formula

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
    "ISLOGICAL" to
      formulaFunction(Param(Scalar)) { args -> BooleanValue(args.value(0) is BooleanValue) },
    "ISNA" to
      formulaFunction(Param(Scalar)) { args ->
        val value = args.value(0)
        BooleanValue(value is ErrorValue && value.type == NotAvailable)
      },
  )
