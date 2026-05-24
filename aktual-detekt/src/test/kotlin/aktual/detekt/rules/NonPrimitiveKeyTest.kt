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
        items(list, key = { it.intVal }) {}
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
        items(list, key = { it.longVal }) {}
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
        items(list, key = { it.stringVal }) {}
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
        items(list, key = { it.dataClassVal }) {}
      }
    """
          .trimIndent()
      )
      .hasSize(1)

  @Test
  fun `reports when key returns a value class`() =
    assertThatLinted(
        """
      $PREAMBLE
      fun foo() {
        items(list, key = { it.valueClassVal }) {}
      }
    """
          .trimIndent()
      )
      .hasSize(1)

  @Test
  fun `reports when key returns an interface type`() =
    assertThatLinted(
        """
      $PREAMBLE
      fun foo() {
        items(list, key = { it.interfaceVal }) {}
      }
    """
          .trimIndent()
      )
      .hasSize(1)

  @Test
  fun `reports when key returns an object`() =
    assertThatLinted(
        """
      $PREAMBLE
      fun foo() {
        items(list, key = { it.objectVal }) {}
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

      @JvmInline
      value class ItemId(val value: String)

      interface Marker

      class MarkerImpl : Marker

      object Singleton

      data class Item(
        val intVal: Int,
        val longVal: Long,
        val stringVal: String,
        val dataClassVal: Tag,
        val valueClassVal: ItemId,
        val interfaceVal: Marker,
        val objectVal: Singleton,
      )

      val list = listOf(Item(1, 1L, "a", Tag("x"), ItemId("id"), MarkerImpl(), Singleton))
      """
        .trimIndent()
  }
}
