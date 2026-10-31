package aktual.detekt.rules

import assertk.Assert
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import dev.detekt.api.Finding
import dev.detekt.test.lint
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

internal class StringPropNameTest {
  private val rule = StringPropName(empty)

  @Test
  fun `reports name string`() =
    assertThatLinted(
        """
        fun test() {
          assertThat(foo).prop("bar") { it.bar }.isEqualTo(1)
        }
        """
          .trimIndent(),
      )
      .hasSize(1)

  @Test
  fun `reports name string inside all`() =
    assertThatLinted(
        """
        fun test() {
          assertThat(foo).all {
            prop("bar") { it.bar }.isEqualTo(1)
            prop("baz") { it.baz }.isEqualTo(2)
          }
        }
        """
          .trimIndent(),
      )
      .hasSize(2)

  @Test
  fun `doesn't report property reference`() =
    assertThatLinted(
        """
        fun test() {
          assertThat(foo).prop(Foo::bar).isEqualTo(1)
        }
        """
          .trimIndent(),
      )
      .isEmpty()

  private fun assertThatLinted(@Language("kotlin") code: String): Assert<List<Finding>> =
    assertThat(rule.lint(code))
}
