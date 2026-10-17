package aktual.budget.formula

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

// Expected values come from running the same formulas through HyperFormula with upstream's config
class ConditionalFunctionsTest {
  @Test
  fun `COUNTIF counts the values matching a criterion`() = runTest {
    assertThat(evaluate("""=COUNTIF({1,2,3,"a"},">1")""")).isEqualTo(NumberValue(2.0))
    assertThat(evaluate("""=COUNTIF({1,2,3},"<>2")""")).isEqualTo(NumberValue(2.0))
    assertThat(evaluate("""=COUNTIF({1,2,3},"<2")""")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("""=COUNTIF({1,2,3},">=2")""")).isEqualTo(NumberValue(2.0))
    assertThat(evaluate("""=COUNTIF({1,2,3},"<=2")""")).isEqualTo(NumberValue(2.0))
    assertThat(evaluate("""=COUNTIF({1,2},"=1")""")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("=COUNTIF({1,2},2.0000000000001)")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("=COUNTIF(5,5)")).isEqualTo(NumberValue(1.0))
  }

  @Test
  fun `COUNTIF matches text that reads as a number`() = runTest {
    assertThat(evaluate("""=COUNTIF({"1",1,TRUE()},1)""")).isEqualTo(NumberValue(2.0))
    assertThat(evaluate("""=COUNTIF({"x",1,TRUE()},"<>1")""")).isEqualTo(NumberValue(2.0))
    assertThat(evaluate("""=COUNTIF({"2",2,""},">1")""")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("""=COUNTIF({1,2},"> 1")""")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("""=COUNTIF({1,2},"a>1")""")).isEqualTo(NumberValue(1.0))
  }

  @Test
  fun `COUNTIF matches text with wildcards`() = runTest {
    assertThat(evaluate("""=COUNTIF({"apple","banana","Apricot"},"a*")"""))
      .isEqualTo(NumberValue(2.0))
    assertThat(evaluate("""=COUNTIF({"cat","coat"},"c?t")""")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("""=COUNTIF({"cat","cot","c?t"},"c~?t")""")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("""=COUNTIF({"a.b","axb"},"a.b")""")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("""=COUNTIF({"Élan","elan"},"elan")""")).isEqualTo(NumberValue(2.0))
    assertThat(evaluate("""=COUNTIF({"a","b"},"<>a")""")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("""=COUNTIF({1,"x"},"*")""")).isEqualTo(NumberValue(1.0))
  }

  @Test
  fun `COUNTIF matches booleans and empty values`() = runTest {
    assertThat(evaluate("=COUNTIF({TRUE(),1},TRUE())")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("""=COUNTIF({TRUE(),"true"},"true")""")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("""=COUNTIF({TRUE(),FALSE()},"<>false")""")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("""=COUNTIF({1,2},"=")""")).isEqualTo(NumberValue(0.0))
    assertThat(evaluate("""=COUNTIF({1,2},"<>")""")).isEqualTo(NumberValue(2.0))
    assertThat(evaluate("""=COUNTIF({1,2},">")""")).isEqualTo(NumberValue(0.0))
    assertThat(evaluate("""=COUNTIF({"",1},"")""")).isEqualTo(NumberValue(0.0))
  }

  @Test
  fun `COUNTIF rejects invalid criteria`() = runTest {
    assertThat(evaluate("""=COUNTIF({1,2},">a")""")).isError(Value, BadCriterion)
    assertThat(evaluate("""=COUNTIF({1,2},"=>1")""")).isError(Value, BadCriterion)
    assertThat(evaluate("=COUNTIF({1,2},)")).isError(Value, BadCriterion)
    assertThat(evaluate("=COUNTIF({1,2},#DIV/0!)")).isError(DivByZero)
    assertThat(evaluate("=COUNTIF({1,#DIV/0!},1)")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("""=COUNTIF({1,#DIV/0!},"<>1")""")).isEqualTo(NumberValue(1.0))
  }

  @Test
  fun `COUNTIFS needs every criterion to match`() = runTest {
    assertThat(evaluate("""=COUNTIFS({1,2,3},">=2",{4,5,6},"<6")""")).isEqualTo(NumberValue(1.0))
    assertThat(evaluate("""=COUNTIFS({1,2,3},">0")""")).isEqualTo(NumberValue(3.0))
    assertThat(evaluate("""=COUNTIFS({1,2},">0",{1,2,3},">0")""")).isError(Value, ArraySizeMismatch)
    assertThat(evaluate("""=COUNTIFS({1,2},">0",{1,2})""")).isError(NotAvailable, WrongArgCount)
  }

  @Test
  fun `SUMIF adds up the values matching a criterion`() = runTest {
    assertThat(evaluate("""=SUMIF({1,2,3},">1")""")).isEqualTo(NumberValue(5.0))
    assertThat(evaluate("""=SUMIF({1,2,3},">1",{10,20,30})""")).isEqualTo(NumberValue(50.0))
    assertThat(evaluate("""=SUMIF({"a","b","a"},"a",{1,2,3})""")).isEqualTo(NumberValue(4.0))
    assertThat(evaluate("""=SUMIF({"a","b"},"a")""")).isEqualTo(NumberValue(0.0))
    assertThat(evaluate("""=SUMIF({1,2,3},">1",{10,"x",TRUE()})""")).isEqualTo(NumberValue(0.0))
    assertThat(evaluate("""=SUMIF({1,2},"")""")).isEqualTo(NumberValue(0.0))
  }

  @Test
  fun `SUMIF only gives errors from matching values`() = runTest {
    assertThat(evaluate("""=SUMIF({1,2,3},">1",{10,#DIV/0!,30})""")).isError(DivByZero)
    assertThat(evaluate("""=SUMIF({1,2,3},">2",{10,#DIV/0!,30})""")).isEqualTo(NumberValue(30.0))
    assertThat(evaluate("""=SUMIF({1,2,3},">1",{10,20})""")).isError(Value, ArraySizeMismatch)
  }

  @Test
  fun `SUMIFS adds up the values matching every criterion`() = runTest {
    assertThat(evaluate("""=SUMIFS({10,20,30},{1,2,3},">1",{"a","b","a"},"a")"""))
      .isEqualTo(NumberValue(30.0))
    assertThat(evaluate("""=SUMIFS({10,20,30},{1,2,3},">1")""")).isEqualTo(NumberValue(50.0))
    assertThat(evaluate("""=SUMIFS({10,20,30},{1,2},">1")""")).isError(Value, ArraySizeMismatch)
    assertThat(evaluate("=SUMIFS({10,20,30},{1,2,3})")).isError(NotAvailable, WrongArgCount)
  }
}
