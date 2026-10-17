package aktual.budget.formula

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

// Expected values come from running the same formulas through HyperFormula with upstream's config
class LogicalFunctionsTest {
  @Test
  fun `IF picks a branch and defaults to false`() = runTest {
    assertThat(evaluate("""=IF(1,"a")""")).isEqualTo(TextValue("a"))
    assertThat(evaluate("""=IF(0,"a")""")).isEqualTo(BooleanValue(false))
    assertThat(evaluate("=IF(TRUE(),{1,2},3)")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("""=IF("a",1,2)""")).isError(Value, WrongArgType)
  }

  @Test
  fun `IF only returns errors from the branch it picks`() = runTest {
    assertThat(evaluate("=IF(1/0,1,2)")).isError(DivByZero)
    assertThat(evaluate("=IF(1,1/0,2)")).isError(DivByZero)
    assertThat(evaluate("=IF(0,1/0,2)")).isEqualTo(NumberValue(2.0))
  }

  @Test
  fun `IFS returns the value of the first true condition`() = runTest {
    assertThat(evaluate("=IFS(0,1,1,2)")).isEqualTo(NumberValue(2.0))
    assertThat(evaluate("=IFS(0,1)")).isError(NotAvailable, NoConditionMet)
    assertThat(evaluate("=IFS(0,1,1)")).isError(NotAvailable, WrongArgCount)
  }

  @Test
  fun `AND, OR and XOR skip array values that aren't booleans`() = runTest {
    assertThat(evaluate("""=AND({1,"a",0})""")).isEqualTo(BooleanValue(false))
    assertThat(evaluate("""=AND({"a","b"})""")).isEqualTo(BooleanValue(true))
    assertThat(evaluate("=AND(,1)")).isEqualTo(BooleanValue(false))
    assertThat(evaluate("=OR(0,0)")).isEqualTo(BooleanValue(false))
    assertThat(evaluate("=XOR(1,1,1)")).isEqualTo(BooleanValue(true))
    assertThat(evaluate("=XOR({1,1})")).isEqualTo(BooleanValue(false))
    assertThat(evaluate("=NOT(0)")).isEqualTo(BooleanValue(true))
  }

  @Test
  fun `A single value array is a scalar`() = runTest {
    assertThat(evaluate("""=AND("a")""")).isError(Value, WrongArgType)
    assertThat(evaluate("""=AND({"a"})""")).isError(Value, WrongArgType)
    assertThat(evaluate("=AND()")).isError(NotAvailable, WrongArgCount)
  }

  @Test
  fun `SWITCH matches cases like the equals operator`() = runTest {
    assertThat(evaluate("""=SWITCH(2,1,"a",2,"b")""")).isEqualTo(TextValue("b"))
    assertThat(evaluate("""=SWITCH(3,1,"a","d")""")).isEqualTo(TextValue("d"))
    assertThat(evaluate("""=SWITCH(3,1,"a")""")).isError(NotAvailable, NoDefault)
    assertThat(evaluate("""=SWITCH(1/0,1,"a")""")).isError(DivByZero)
    assertThat(evaluate("""=SWITCH("A",1/0,"x","a","y")""")).isEqualTo(TextValue("y"))
    assertThat(evaluate("=SWITCH(1,1)")).isError(NotAvailable, WrongArgCount)
  }

  @Test
  fun `IFERROR and IFNA replace errors`() = runTest {
    assertThat(evaluate("""=IFERROR(1/0,"x")""")).isEqualTo(TextValue("x"))
    assertThat(evaluate("""=IFERROR(1,"x")""")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("""=IFNA(#N/A,"x")""")).isEqualTo(TextValue("x"))
    assertThat(evaluate("""=IFNA(1/0,"x")""")).isError(DivByZero)
  }

  @Test
  fun `Info functions check the value's type`() = runTest {
    assertThat(evaluate("""=ISNUMBER("1")""")).isEqualTo(BooleanValue(false))
    assertThat(evaluate("""=ISNUMBER({1,"a"})""")).isEqualTo(BooleanValue(true))
    assertThat(evaluate("""=ISTEXT("")""")).isEqualTo(BooleanValue(true))
    assertThat(evaluate("=ISBLANK(B1)")).isEqualTo(BooleanValue(true))
    assertThat(evaluate("""=ISBLANK("")""")).isEqualTo(BooleanValue(false))
    assertThat(evaluate("=ISBLANK(,)")).isError(NotAvailable, WrongArgCount)
    assertThat(evaluate("=ISERROR({1/0,1})")).isEqualTo(BooleanValue(true))
  }

  @Test
  fun `ISNA and ISLOGICAL check the value's type`() = runTest {
    assertThat(evaluate("=ISNA(#N/A)")).isEqualTo(BooleanValue(true))
    assertThat(evaluate("=ISNA(#VALUE!)")).isEqualTo(BooleanValue(false))
    assertThat(evaluate("=ISNA(1)")).isEqualTo(BooleanValue(false))
    assertThat(evaluate("=ISLOGICAL(TRUE())")).isEqualTo(BooleanValue(true))
    assertThat(evaluate("=ISLOGICAL(1)")).isEqualTo(BooleanValue(false))
    assertThat(evaluate("""=ISLOGICAL("TRUE")""")).isEqualTo(BooleanValue(false))
  }

  @Test
  fun `CHOOSE picks an argument by index`() = runTest {
    assertThat(evaluate("""=CHOOSE(2,"a","b","c")""")).isEqualTo(TextValue("b"))
    assertThat(evaluate("""=CHOOSE("2","a","b")""")).isEqualTo(TextValue("b"))
    assertThat(evaluate("""=CHOOSE(2,#N/A,"b")""")).isEqualTo(TextValue("b"))
    assertThat(evaluate("""=CHOOSE(1,#N/A,"b")""")).isError(NotAvailable)
    assertThat(evaluate("""=CHOOSE(4,"a","b","c")""")).isError(Num, SelectorTooLarge)
    assertThat(evaluate("""=CHOOSE(0,"a")""")).isError(Num, ValueTooSmall)
    assertThat(evaluate("""=CHOOSE(1.5,"a","b")""")).isError(Num, IntegerExpected)
    assertThat(evaluate("=CHOOSE(1)")).isError(NotAvailable, WrongArgCount)
  }

  @Test
  fun `ISREF checks for reference errors`() = runTest {
    assertThat(evaluate("=ISREF(#REF!)")).isEqualTo(BooleanValue(true))
    assertThat(evaluate("=ISREF(1)")).isEqualTo(BooleanValue(false))
    assertThat(evaluate("=ISREF(B2)")).isEqualTo(BooleanValue(false))
    assertThat(evaluate("=ISREF(#N/A)")).isEqualTo(BooleanValue(false))
  }
}
