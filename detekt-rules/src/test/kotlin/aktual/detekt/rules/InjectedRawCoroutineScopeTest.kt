package aktual.detekt.rules

import assertk.Assert
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import dev.detekt.api.Config
import dev.detekt.api.Finding
import dev.detekt.test.lint
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

internal class InjectedRawCoroutineScopeTest {
  private val rule = InjectedRawCoroutineScope(Config.empty)

  @Test
  fun `reports raw scope in an Inject class`() =
    assertThatLinted(
        """
        @Inject
        class Foo(private val scope: CoroutineScope)
        """
          .trimIndent()
      )
      .hasSize(1)

  @Test
  fun `reports raw scope in an Inject constructor`() =
    assertThatLinted(
        """
        class Foo @Inject constructor(private val scope: CoroutineScope)
        """
          .trimIndent()
      )
      .hasSize(1)

  @Test
  fun `reports fully qualified raw scope`() =
    assertThatLinted(
        """
        @Inject
        class Foo(private val scope: kotlinx.coroutines.CoroutineScope)
        """
          .trimIndent()
      )
      .hasSize(1)

  @Test
  fun `reports raw scope in a contributed class`() =
    assertThatLinted(
        """
        @SingleIn(BudgetScope::class)
        @ContributesBinding(BudgetScope::class)
        class Foo(private val scope: CoroutineScope) : Bar
        """
          .trimIndent()
      )
      .hasSize(1)

  @Test
  fun `reports raw scope in an injected secondary constructor`() =
    assertThatLinted(
        """
        @ContributesBinding(BudgetScope::class)
        class Foo private constructor(private val scope: AppCoroutineScope, private val x: Int) : Bar {
          @Inject constructor(scope: CoroutineScope) : this(AppCoroutineScope(scope), 0)
        }
        """
          .trimIndent()
      )
      .hasSize(1)

  @Test
  fun `reports raw scope in a Provides function`() =
    assertThatLinted(
        """
        object Container {
          @Provides fun foo(scope: CoroutineScope): Foo = Foo(scope)
        }
        """
          .trimIndent()
      )
      .hasSize(1)

  @Test
  fun `does not report scoped types`() =
    assertThatLinted(
        """
        @Inject
        class Foo(private val app: AppCoroutineScope, private val budget: BudgetCoroutineScope)
        """
          .trimIndent()
      )
      .isEmpty()

  @Test
  fun `does not report classes outside DI`() =
    assertThatLinted(
        """
        class Foo(private val scope: CoroutineScope)
        """
          .trimIndent()
      )
      .isEmpty()

  @Test
  fun `does not report non-provider functions`() =
    assertThatLinted(
        """
        fun foo(scope: CoroutineScope) = Unit
        """
          .trimIndent()
      )
      .isEmpty()

  @Test
  fun `does not report a Provides function returning a raw scope`() =
    assertThatLinted(
        """
        object Container {
          @Provides fun scope(): CoroutineScope = MainScope()
        }
        """
          .trimIndent()
      )
      .isEmpty()

  private fun assertThatLinted(@Language("kotlin") code: String): Assert<List<Finding>> =
    assertThat(rule.lint(code))
}
