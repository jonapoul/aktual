package aktual.detekt.rules

import assertk.Assert
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import dev.detekt.api.Config
import dev.detekt.api.Finding
import dev.detekt.test.junit.KotlinCoreEnvironmentTest
import dev.detekt.test.utils.KotlinAnalysisApiEngine
import dev.detekt.test.utils.KotlinEnvironmentContainer
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl
import org.junit.jupiter.api.Test

@KotlinCoreEnvironmentTest
internal class NonPrimitiveKeyTest(private val env: KotlinEnvironmentContainer) {
  private val rule = NonPrimitiveKey(Config.empty)

  @Test
  fun `does not report when key returns Int`() =
    assertThatLinted(
        """
      $PREAMBLE
      fun foo() {
        items(list, key = { it.id }) {}
      }
    """
          .trimIndent()
      )
      .isEmpty()

  @Test
  fun `does not report when key returns Long`() =
    assertThatLinted(
        """
      $PREAMBLE
      fun foo() {
        items(list, key = { it.serial }) {}
      }
    """
          .trimIndent()
      )
      .isEmpty()

  @Test
  fun `does not report when key returns String`() =
    assertThatLinted(
        """
      $PREAMBLE
      fun foo() {
        items(list, key = { it.name }) {}
      }
    """
          .trimIndent()
      )
      .isEmpty()

  @Test
  fun `does not report when there is no key argument`() =
    assertThatLinted(
        """
      $PREAMBLE
      fun foo() {
        items(list) {}
      }
    """
          .trimIndent()
      )
      .isEmpty()

  @Test
  fun `reports when key returns a data class`() =
    assertThatLinted(
        """
      $PREAMBLE
      fun foo() {
        items(list, key = { it.tag }) {}
      }
    """
          .trimIndent()
      )
      .hasSize(1)

  @Test
  fun `reports when key returns the item itself`() =
    assertThatLinted(
        """
      $PREAMBLE
      fun foo() {
        items(list, key = { it }) {}
      }
    """
          .trimIndent()
      )
      .hasSize(1)

  private fun assertThatLinted(@Language("kotlin") code: String): Assert<List<Finding>> {
    val findings =
      KotlinAnalysisApiEngine().use { engine ->
        val ktFile =
          engine.compile(
            code = code,
            javaSourceRoots = env.javaSourceRoots,
            jvmClasspathRoots = env.jvmClasspathRoots,
            allowCompilationErrors = false,
          )
        rule.visitFile(ktFile, LanguageVersionSettingsImpl.DEFAULT)
      }
    return assertThat(findings)
  }

  private companion object {
    @Language("kotlin")
    val PREAMBLE =
      """
      fun <T> items(list: List<T>, key: ((T) -> Any)? = null, content: (T) -> Unit) {}

      data class Tag(val value: String)

      data class Item(val id: Int, val serial: Long, val name: String, val tag: Tag)

      val list = listOf(Item(1, 1L, "a", Tag("x")))
      """
        .trimIndent()
  }
}
