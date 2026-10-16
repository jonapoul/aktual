package aktual.budget.formula.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

// Expected values come from running the same formulas through HyperFormula with upstream's config
class TextFunctionsTest {
  @Test
  fun `CONCATENATE joins values as text`() = runTest {
    assertThat(evaluate("""=CONCATENATE(1.5,TRUE(),,"x")""")).isEqualTo(TextValue("1.5TRUEx"))
    assertThat(evaluate("""=CONCATENATE({1,"a"},TRUE(),2.50)""")).isEqualTo(TextValue("1aTRUE2.5"))
    assertThat(evaluate("""=CONCATENATE(1/0,"x")""")).isError(DivByZero)
    assertThat(evaluate("=CONCATENATE()")).isError(NotAvailable, WrongArgCount)
  }

  @Test
  fun `Case, length and trimming`() = runTest {
    assertThat(evaluate("=LEN(123.5)")).isEqualTo(NumberValue(5.0))
    assertThat(evaluate("""=UPPER("ß")""")).isEqualTo(TextValue("SS"))
    assertThat(evaluate("""=LOWER("ABC")""")).isEqualTo(TextValue("abc"))
    assertThat(evaluate("""=TRIM("  a   b  ")""")).isEqualTo(TextValue("a b"))
  }

  @Test
  fun `LEFT, RIGHT and MID truncate fractional lengths`() = runTest {
    assertThat(evaluate("""=LEFT("abc")""")).isEqualTo(TextValue("a"))
    assertThat(evaluate("""=LEFT("abc",1.9)""")).isEqualTo(TextValue("a"))
    assertThat(evaluate("""=LEFT("abc",-1)""")).isError(Value, NegativeLength)
    assertThat(evaluate("""=RIGHT("abc",1.9)""")).isEqualTo(TextValue("c"))
    assertThat(evaluate("""=RIGHT("abc",0)""")).isEqualTo(TextValue(""))
    assertThat(evaluate("""=RIGHT("abc",5)""")).isEqualTo(TextValue("abc"))
    assertThat(evaluate("""=MID("abcdef",2.5,2.5)""")).isEqualTo(TextValue("bcd"))
    assertThat(evaluate("""=MID("abc",0,1)""")).isError(Value, LessThanOne)
    assertThat(evaluate("""=MID("abc",5,1)""")).isEqualTo(TextValue(""))
  }

  @Test
  fun `REPT repeats a whole number of times`() = runTest {
    assertThat(evaluate("""=REPT("ab",2.9)""")).isEqualTo(TextValue("abab"))
    assertThat(evaluate("""=REPT("ab",0)""")).isEqualTo(TextValue(""))
    assertThat(evaluate("""=REPT("ab",-1)""")).isError(Value, NegativeCount)
    assertThat(evaluate("""=REPT("ab",1e9)""")).isError(Value, TextTooLong)
  }

  @Test
  fun `SUBSTITUTE replaces every match`() = runTest {
    assertThat(evaluate("""=SUBSTITUTE("a.c",".","x")""")).isEqualTo(TextValue("axc"))
    assertThat(evaluate("""=SUBSTITUTE("aaa","aa","x")""")).isEqualTo(TextValue("xa"))
    assertThat(evaluate("""=SUBSTITUTE("abc","","x")""")).isEqualTo(TextValue("xaxbxcx"))
  }

  @Test
  fun `SUBSTITUTE expands JavaScript replacement patterns`() = runTest {
    assertThat(evaluate("""=SUBSTITUTE("abcb","b","$&$&")""")).isEqualTo(TextValue("abbcbb"))
    assertThat(evaluate("""=SUBSTITUTE("abc","b","$$")""")).isEqualTo(TextValue("a\$c"))
    assertThat(evaluate("""=SUBSTITUTE("abc","b","$`")""")).isEqualTo(TextValue("aac"))
    assertThat(evaluate("""=SUBSTITUTE("abc","b","$'")""")).isEqualTo(TextValue("acc"))
    assertThat(evaluate("""=SUBSTITUTE("abc","b","$1")""")).isEqualTo(TextValue("a$1c"))
  }

  @Test
  fun `SUBSTITUTE replaces one occurrence`() = runTest {
    assertThat(evaluate("""=SUBSTITUTE("abcb","b","x",2)""")).isEqualTo(TextValue("abcx"))
    assertThat(evaluate("""=SUBSTITUTE("abcb","b","x",3)""")).isEqualTo(TextValue("abcb"))
    assertThat(evaluate("""=SUBSTITUTE("abcb","b","x",1.5)""")).isEqualTo(TextValue("abcb"))
    assertThat(evaluate("""=SUBSTITUTE("abcb","b","x",0)""")).isError(Value, LessThanOne)
    assertThat(evaluate("""=SUBSTITUTE("aaa","aa","x",2)""")).isEqualTo(TextValue("aaa"))
    assertThat(evaluate("""=SUBSTITUTE("abc","","x",2)""")).isEqualTo(TextValue("xabc"))
  }
}
