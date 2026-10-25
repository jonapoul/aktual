package aktual.detekt.rules

import assertk.Assert
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import dev.detekt.api.Finding
import dev.detekt.test.lint
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

internal class SetMainDispatcherTest {
  private val rule = SetMainDispatcher(empty)

  @Test
  fun `reports setMain`() =
    assertThatLinted(
        """
        fun before() {
          Dispatchers.setMain(StandardTestDispatcher())
        }
        """
          .trimIndent(),
      )
      .hasSize(1)

  @Test
  fun `reports unqualified setMain`() =
    assertThatLinted(
        """
        fun Dispatchers.before(dispatcher: TestDispatcher) {
          setMain(dispatcher)
        }
        """
          .trimIndent(),
      )
      .hasSize(1)

  @Test
  fun `doesn't report other calls`() =
    assertThatLinted(
        """
        fun before() {
          val dispatcher = StandardTestDispatcher()
          Dispatchers.resetMain()
        }
        """
          .trimIndent(),
      )
      .isEmpty()

  private fun assertThatLinted(@Language("kotlin") code: String): Assert<List<Finding>> =
    assertThat(rule.lint(code))
}
