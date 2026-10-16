package aktual.budget.formula.domain

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class FormulaFunctionTest {
  @Test
  fun `Argument count is checked`() = runTest {
    assertThat(evaluate("=TRUE(1)")).isError(NotAvailable, WRONG_ARG_NUMBER_MESSAGE)
    assertThat(evaluate("=ECHO()")).isError(NotAvailable, WRONG_ARG_NUMBER_MESSAGE)
    assertThat(evaluate("=ECHO(1,2)")).isError(NotAvailable, WRONG_ARG_NUMBER_MESSAGE)
  }

  @Test
  fun `Missing optional arguments use their default`() = runTest {
    assertThat(evaluate("=ADD(1)")).isEqualTo(NumberValue(3.0))
    assertThat(evaluate("=ADD(1,5)")).isEqualTo(NumberValue(6.0))
  }

  @Test
  fun `An explicitly empty argument is empty, not the default`() = runTest {
    assertThat(evaluate("=ADD(1,)")).isEqualTo(NumberValue(1.0))
  }

  @Test
  fun `Arguments are coerced to their parameter type`() = runTest {
    assertThat(evaluate("=ADD(\"1\",TRUE())")).isEqualTo(NumberValue(2.0))
    assertThat(evaluate("=ADD(\"x\")")).isError(FormulaErrorType.Value, NUMBER_COERCION_MESSAGE)
    assertThat(evaluate("=SHOUT(12)")).isEqualTo(TextValue("12!"))
    assertThat(evaluate("=NEGATE(\"true\")")).isEqualTo(BooleanValue(false))
    assertThat(evaluate("=NEGATE(\"yes\")"))
      .isError(FormulaErrorType.Value, "Wrong type of argument.")
  }

  @Test
  fun `Error arguments are returned, except for scalar parameters`() = runTest {
    assertThat(evaluate("=ADD(1/0)")).isError(DivByZero)
    assertThat(evaluate("=ECHO(1/0)")).isError(DivByZero)
    assertThat(evaluate("=ISERR(1/0)")).isEqualTo(BooleanValue(true))
  }

  @Test
  fun `Arrays pass to scalar parameters as their top left value`() = runTest {
    assertThat(evaluate("=ADD({5,6})")).isEqualTo(NumberValue(7.0))
  }

  @Test
  fun `Bounds and integers are checked`() = runTest {
    assertThat(evaluate("=INDEXED(1)")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("=INDEXED(0)")).isError(Num, "Value too small.")
    assertThat(evaluate("=INDEXED(11)")).isError(Num, "Value too large.")
    assertThat(evaluate("=INDEXED(1.5)")).isError(Num, "Value needs to be an integer.")
  }

  @Test
  fun `Last arguments repeat and arrays expand`() = runTest {
    assertThat(evaluate("=TOTAL()")).isError(NotAvailable)
    assertThat(evaluate("=TOTAL(1)")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("=TOTAL(1,2,3)")).isEqualTo(NumberValue(6.0))
    assertThat(evaluate("=TOTAL({1,2;3,4},5)")).isEqualTo(NumberValue(15.0))
  }

  @Test
  fun `Functions can add warnings`() = runTest {
    val result = FormulaEvaluator(FUNCTIONS).evaluate("=WARN(\"a\")+WARN(\"b\")")
    assertThat(result.value).isEqualTo(NumberValue(0.0))
    assertThat(result.warnings).containsExactly(TestWarning("a"), TestWarning("b"))
  }

  private data class TestWarning(val text: String) : FormulaWarning

  private suspend fun evaluate(formula: String): FormulaValue =
    FormulaEvaluator(FUNCTIONS).evaluate(formula).value

  private companion object {
    val FUNCTIONS =
      CORE_FUNCTIONS +
        mapOf(
          "ECHO" to formulaFunction(Param(Scalar)) { args -> args[0]!! },
          "ADD" to
            formulaFunction(Param(Numeric), Param(Numeric, default = NumberValue(2.0))) { args ->
              NumberValue(args.number(0) + args.number(1))
            },
          "SHOUT" to formulaFunction(Param(ArgType.Text)) { args -> TextValue(args.text(0) + "!") },
          "NEGATE" to formulaFunction(Param(Logical)) { args -> BooleanValue(!args.boolean(0)) },
          "ISERR" to formulaFunction(Param(Scalar)) { args -> BooleanValue(args[0] is ErrorValue) },
          "INDEXED" to
            formulaFunction(Param(Integral, minValue = 1.0, maxValue = 10.0)) { args ->
              NumberValue(args.number(0))
            },
          "TOTAL" to
            formulaFunction(Param(Numeric), repeatLastArgs = 1, expandRanges = true) { args ->
              NumberValue((0 until args.size).sumOf { args.number(it) })
            },
          "WARN" to
            formulaFunction(Param(ArgType.Text)) { args ->
              warn(TestWarning(args.text(0)))
              NumberValue(0.0)
            },
        )
  }
}
