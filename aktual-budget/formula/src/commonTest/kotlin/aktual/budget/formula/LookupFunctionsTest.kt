package aktual.budget.formula

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

// Expected values come from running the same formulas through HyperFormula with upstream's config
class LookupFunctionsTest {
  @Test
  fun `INDEX picks a value by position`() = runTest {
    assertThat(evaluate("=INDEX({1,2;3,4},2,2)")).isEqualTo(NumberValue(4.0))
    assertThat(evaluate("=INDEX({1,2;3,4},2)")).isEqualTo(NumberValue(3.0))
    assertThat(evaluate("=INDEX({1,2,3},1,3)")).isEqualTo(NumberValue(3.0))
    assertThat(evaluate("=INDEX(5,1,1)")).isEqualTo(NumberValue(5.0))
    assertThat(evaluate("=INDEX({1,2;3,4},2,1.5)")).isEqualTo(NumberValue(1.0))
  }

  @Test
  fun `INDEX rejects positions outside the array`() = runTest {
    assertThat(evaluate("=INDEX({1,2;3,4},3,1)")).isError(Num, ValueTooLarge)
    assertThat(evaluate("=INDEX({1,2;3,4},1,3)")).isError(Num, ValueTooLarge)
    assertThat(evaluate("=INDEX({1,2;3,4},0,1)")).isError(Value, LessThanOne)
    assertThat(evaluate("""=INDEX({1,2;3,4},"x",1)""")).isError(Value, NumberCoercion)
  }

  @Test
  fun `MATCH finds an exact position`() = runTest {
    assertThat(evaluate("=MATCH(5,{1,2,3},0)")).isError(NotAvailable, ValueNotFound)
    assertThat(evaluate("""=MATCH("B",{"a","b"},0)""")).isEqualTo(NumberValue(2.0))
    assertThat(evaluate("""=MATCH(TRUE(),{1,"a",TRUE()},0)""")).isEqualTo(NumberValue(3.0))
    assertThat(evaluate("""=MATCH(2,{"2",2},0)""")).isEqualTo(NumberValue(2.0))
    assertThat(evaluate("=MATCH(2.0000000000001,{1,2},0)")).isError(NotAvailable, ValueNotFound)
    assertThat(evaluate("=MATCH(,{0,1},0)")).isEqualTo(NumberValue(1.0))
  }

  @Test
  fun `MATCH finds the closest position`() = runTest {
    assertThat(evaluate("=MATCH(2,{1,2,3})")).isEqualTo(NumberValue(2.0))
    assertThat(evaluate("=MATCH(2.5,{1,2,3})")).isEqualTo(NumberValue(2.0))
    assertThat(evaluate("=MATCH(2.5,{3,1,2})")).isEqualTo(NumberValue(3.0))
    assertThat(evaluate("=MATCH(0,{1,2,3})")).isError(NotAvailable, ValueNotFound)
    assertThat(evaluate("""=MATCH("b",{1,"a","c"})""")).isEqualTo(NumberValue(2.0))
    assertThat(evaluate("=MATCH(2,{3,2,1},-1)")).isEqualTo(NumberValue(2.0))
    assertThat(evaluate("=MATCH(1.5,{3,2,1},-1)")).isEqualTo(NumberValue(2.0))
    assertThat(evaluate("=MATCH(4,{3,2,1},-1)")).isError(NotAvailable, ValueNotFound)
  }

  @Test
  fun `MATCH rejects bad arguments`() = runTest {
    assertThat(evaluate("=MATCH(2,{1,2,3},2)")).isError(Value, BadMode)
    assertThat(evaluate("=MATCH(2,{1,2;3,4})")).isError(NotAvailable)
    assertThat(evaluate("=MATCH(#N/A,{1})")).isError(NotAvailable)
  }

  @Test
  fun `VLOOKUP and HLOOKUP need a cell range`() = runTest {
    assertThat(evaluate("""=VLOOKUP(2,{1,"a";2,"b"},2)""")).isError(Value, WrongArgType)
    assertThat(evaluate("""=VLOOKUP(2,{1,"a";2,"b"},2,FALSE())""")).isError(Value, WrongArgType)
    assertThat(evaluate("""=HLOOKUP(2,{1,2;"a","b"},2)""")).isError(Value, WrongArgType)
    assertThat(evaluate("""=VLOOKUP(#N/A,{1,"a"},2)""")).isError(NotAvailable)
  }
}
