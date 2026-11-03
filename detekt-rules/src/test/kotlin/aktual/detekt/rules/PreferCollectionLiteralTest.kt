package aktual.detekt.rules

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import dev.detekt.api.Finding
import dev.detekt.test.junit.KotlinCoreEnvironmentTest
import dev.detekt.test.utils.KotlinAnalysisApiEngine
import dev.detekt.test.utils.KotlinEnvironmentContainer
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.config.LanguageFeature.CollectionLiterals
import org.jetbrains.kotlin.config.LanguageFeature.State.ENABLED
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl
import org.junit.jupiter.api.Test

@KotlinCoreEnvironmentTest
internal class PreferCollectionLiteralTest(private val env: KotlinEnvironmentContainer) {
  private val rule = PreferCollectionLiteral(empty)

  @Test
  fun `reports listOf with no expected type`() =
    assertReported("val x = listOf(1, 2)", "listOf(1, 2)" to "[1, 2]")

  @Test
  fun `reports listOf as an argument`() =
    assertReported("fun foo() = takesList(listOf(1))", "listOf(1)" to "[1]")

  @Test
  fun `reports listOf as a receiver`() =
    assertReported("val x = listOf(1, 2).map { it * 2 }", "listOf(1, 2)" to "[1, 2]")

  @Test
  fun `reports listOf where Any is expected`() =
    assertReported("val x: Any = listOf(1)", "listOf(1)" to "[1]")

  @Test
  fun `reports listOf where a Collection is expected`() =
    assertReported("val x: Collection<Int> = listOf(1)", "listOf(1)" to "[1]")

  @Test
  fun `reports multiline listOf`() {
    val code =
      """
      val x =
        listOf(
          1,
          2,
        )
      """
        .trimIndent()
    assertThat(lint(PREAMBLE + "\n" + code).map { it.message })
      .containsExactly("'listOf' can be written as a collection literal")
    compile(PREAMBLE + "\n" + code.replace("listOf(", "[").replace("  )", "  ]"))
  }

  @Test
  fun `reports nested listOf`() =
    assertReported(
      "val x = listOf(listOf(1))",
      "listOf(1)" to "[1]",
      "listOf([1])" to "[[1]]",
    )

  @Test
  fun `reports listOf in a constructor argument`() =
    assertReported("val x = Wrapper(listOf(1))", "listOf(1)" to "[1]")

  @Test
  fun `reports listOf in a constructor inside a generic call`() =
    assertReported(
      "val x = listOf(Wrapper(listOf(1)))",
      "listOf(1)" to "[1]",
      "listOf(Wrapper([1]))" to "[Wrapper([1])]",
    )

  @Test
  fun `reports listOf in a constructor inside buildList`() =
    assertReported(
      "val x = buildList { add(Wrapper(listOf(1))) }",
      "listOf(1)" to "[1]",
    )

  @Test
  fun `reports listOf passed to a generic parameter`() =
    assertReported(
      "fun foo() = same(listOf(1), listOf(2))",
      "listOf(1)" to "[1]",
      "listOf(2)" to "[2]",
    )

  @Test
  fun `reports listOf passed to an overloaded generic extension`() =
    assertReported("fun foo(x: List<Int>) = Assert(x).isEqualTo(listOf(1))", "listOf(1)" to "[1]")

  @Test
  fun `reports emptyList passed to an overloaded generic extension`() =
    assertReported(
      "fun foo(x: List<Int>) = Assert(x).isEqualTo(emptyList())",
      "emptyList()" to "[]",
    )

  @Test
  fun `reports emptyList as a typed property initializer`() =
    assertReported("val x: List<Int> = emptyList()", "emptyList()" to "[]")

  @Test
  fun `reports emptyList as a default parameter value`() =
    assertReported("fun foo(x: List<Int> = emptyList()) = x", "emptyList()" to "[]")

  @Test
  fun `reports emptyList as an argument`() =
    assertReported("fun foo() = takesList(emptyList())", "emptyList()" to "[]")

  @Test
  fun `reports emptyList as a return value`() =
    assertReported("fun foo(): List<Int> = emptyList()", "emptyList()" to "[]")

  @Test
  fun `reports emptyList in an if branch`() =
    assertReported(
      "fun foo(b: Boolean) = if (b) listOf(1) else emptyList()",
      "listOf(1)" to "[1]",
      "emptyList()" to "[]",
    )

  @Test
  fun `reports typed emptyList with an expected type`() =
    assertReported("val x: List<Int> = emptyList<Int>()", "emptyList<Int>()" to "[]")

  @Test
  fun `does not report typed listOf with no expected type`() =
    assertNotReported("val x = listOf<Number>(1)")

  @Test
  fun `does not report typed emptyList as a receiver`() =
    assertNotReported("val x = emptyList<Int>().size")

  @Test
  fun `does not report emptyList where Any is expected`() =
    assertNotReported("fun foo() = takesAny(emptyList<Int>())")

  @Test
  fun `does not report emptyList passed to a type parameter of the call`() =
    assertNotReported("val x: Map<String, List<Int>> = mapOf(\"a\" to emptyList())")

  @Test
  fun `does not report emptyList passed to a generic function`() =
    assertNotReported("fun foo(x: List<Int>) = same(emptyList(), x)")

  @Test
  fun `reports setOf where a Set is expected`() =
    assertReported("val x: Set<Int> = setOf(1, 2)", "setOf(1, 2)" to "[1, 2]")

  @Test
  fun `reports setOf as a Set argument`() =
    assertReported("fun foo() = takesSet(setOf(1))", "setOf(1)" to "[1]")

  @Test
  fun `reports emptySet as a default parameter value`() =
    assertReported("fun foo(x: Set<Int> = emptySet()) = x", "emptySet()" to "[]")

  @Test
  fun `does not report setOf passed to a type parameter`() =
    assertNotReported("fun foo(x: Set<Int>) = Assert(x).isEqualTo(setOf(1))")

  @Test
  fun `does not report setOf in a generic infix call`() =
    assertNotReported("val x: Map<String, Set<Int>> = mapOf(\"a\" to setOf(1))")

  @Test
  fun `does not report setOf with no expected type`() = assertNotReported("val x = setOf(1, 2)")

  @Test
  fun `does not report setOf where a Collection is expected`() =
    assertNotReported("val x: Collection<Int> = setOf(1)")

  @Test
  fun `does not report setOf where Any is expected`() =
    assertNotReported("fun foo() = takesAny(setOf(1))")

  @Test
  fun `does not report setOf as a receiver`() = assertNotReported("val x = setOf(1).contains(1)")

  @Test
  fun `does not report spread arguments`() =
    assertNotReported("fun foo(items: Array<Int>) = listOf(*items)")

  @Test
  fun `does not report arguments of overloads that expect different types`() =
    assertNotReported("fun foo() = overloaded(listOf(1))")

  @Test
  fun `does not report an operand of overloaded operators`() =
    assertNotReported("fun foo(x: List<List<Int>>, y: List<Int>) = x + listOf(y)")

  @Test
  fun `reports an operand of an equality check`() =
    assertReported("fun foo(x: List<Int>) = x == listOf(1)", "listOf(1)" to "[1]")

  @Test
  fun `reports an argument where only one overload takes a collection`() =
    assertReported("fun foo() = single(listOf(1))", "listOf(1)" to "[1]")

  @Test
  fun `does not report other functions named listOf`() =
    assertNotReported("val x = model.Other.listOf(1)")

  @Test fun `does not report mutableListOf`() = assertNotReported("val x = mutableListOf(1)")

  @Test
  fun `does not report without the language feature`() =
    assertThat(lint("$PREAMBLE\nval x = listOf(1)", LanguageVersionSettingsImpl.DEFAULT)).isEmpty()

  // Checks the findings, then that the code still compiles once they're applied
  private fun assertReported(@Language("kotlin") code: String, vararg fixes: Pair<String, String>) {
    val source = PREAMBLE + "\n" + code
    val expected = fixes.map { (from, _) ->
      "'${from.substringBefore('(').substringBefore('<')}' can be written as a collection literal"
    }
    val messages = lint(source).map { it.message }
    assertThat(messages).containsExactly(*expected.toTypedArray())
    compile(fixes.fold(source) { acc, (from, to) -> acc.replace(from, to) })
  }

  private fun assertNotReported(@Language("kotlin") code: String) {
    val source = PREAMBLE + "\n" + code
    assertThat(lint(source)).isEmpty()
  }

  private fun lint(code: String, settings: LanguageVersionSettings = LITERALS): List<Finding> =
    KotlinAnalysisApiEngine().use { engine ->
      rule.visitFile(engine.compile(code, settings), settings)
    }

  private fun compile(code: String) {
    KotlinAnalysisApiEngine().use { engine -> engine.compile(code, LITERALS) }
  }

  private fun KotlinAnalysisApiEngine.compile(code: String, settings: LanguageVersionSettings) =
    compile(
      code = code,
      javaSourceRoots = env.javaSourceRoots,
      jvmClasspathRoots = env.jvmClasspathRoots,
      allowCompilationErrors = false,
      languageVersionSettings = settings,
    )

  private companion object {
    val LITERALS =
      LanguageVersionSettingsImpl(
        languageVersion = LATEST_STABLE,
        apiVersion = LATEST_STABLE,
        specificFeatures = mapOf(CollectionLiterals to ENABLED),
      )

    @Language("kotlin")
    val PREAMBLE =
      """
      package model

      fun takesList(list: List<Int>) = list

      fun takesSet(set: Set<Int>) = set

      fun takesAny(any: Any) = any

      fun overloaded(list: List<Int>) = list

      fun overloaded(set: Set<Int>) = set

      fun single(item: Int) = item

      fun single(items: Iterable<Int>) = items

      fun <T> same(a: T, b: T) = a == b

      class Wrapper(val list: List<Int>, val flag: Boolean = false)

      class Assert<out T>(val actual: T)

      fun <T> Assert<T>.isEqualTo(expected: T) = actual == expected

      fun Assert<String?>.isEqualTo(other: String?, ignoreCase: Boolean = false) =
        actual.equals(other, ignoreCase)

      object Other {
        fun listOf(vararg items: Int) = items.toList()
      }
      """
        .trimIndent()
  }
}
