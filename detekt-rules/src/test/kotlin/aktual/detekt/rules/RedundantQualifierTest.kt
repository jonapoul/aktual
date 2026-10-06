package aktual.detekt.rules

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import dev.detekt.api.Finding
import dev.detekt.test.junit.KotlinCoreEnvironmentTest
import dev.detekt.test.utils.KotlinAnalysisApiEngine
import dev.detekt.test.utils.KotlinEnvironmentContainer
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl
import org.junit.jupiter.api.Test

@KotlinCoreEnvironmentTest
internal class RedundantQualifierTest(private val env: KotlinEnvironmentContainer) {
  private val rule = RedundantQualifier(empty)

  @Test
  fun `reports enum entry as a positional argument`() =
    assertReported("fun foo() = takesRole(Role.Admin)", "Role.Admin" to "Admin")

  @Test
  fun `reports enum entry as a named argument`() =
    assertReported("fun foo() = takesRole(role = Role.Admin)", "Role.Admin" to "Admin")

  @Test
  fun `reports enum entry as a constructor argument`() =
    assertReported("fun foo() = User(Role.Admin)", "Role.Admin" to "Admin")

  @Test
  fun `reports enum entry in a when branch`() =
    assertReported(
      """
      fun foo(role: Role) = when (role) {
        Role.Admin -> 1
        Role.Basic -> 2
      }
      """
        .trimIndent(),
      "Role.Admin" to "Admin",
      "Role.Basic" to "Basic",
    )

  @Test
  fun `reports enum entry in a when branch with a subject variable`() =
    assertReported(
      """
      fun foo(user: User) = when (val role = user.role) {
        Role.Admin -> 1
        else -> role.ordinal
      }
      """
        .trimIndent(),
      "Role.Admin" to "Admin",
    )

  @Test
  fun `reports enum entry in an equality check`() =
    assertReported(
      "fun foo(role: Role) = role == Role.Admin || role != Role.Basic",
      "Role.Admin" to "Admin",
      "Role.Basic" to "Basic",
    )

  @Test
  fun `reports enum entry compared to a nullable value`() =
    assertReported("fun foo(role: Role?) = role == Role.Admin", "Role.Admin" to "Admin")

  @Test
  fun `reports enum entry as a typed property initializer`() =
    assertReported("val role: Role = Role.Admin", "Role.Admin" to "Admin")

  @Test
  fun `reports enum entry as a default parameter value`() =
    assertReported("fun foo(role: Role = Role.Admin) = role", "Role.Admin" to "Admin")

  @Test
  fun `reports enum entry as a return value`() =
    assertReported(
      """
      fun foo(): Role {
        return Role.Admin
      }
      """
        .trimIndent(),
      "Role.Admin" to "Admin",
    )

  @Test
  fun `reports enum entry as an expression body`() =
    assertReported("fun foo(): Role = Role.Admin", "Role.Admin" to "Admin")

  @Test
  fun `reports enum entry as an assignment`() =
    assertReported(
      """
      fun foo() {
        var role: Role = makeRole()
        role = Role.Admin
      }
      """
        .trimIndent(),
      "Role.Admin" to "Admin",
    )

  @Test
  fun `reports enum entry in an assertion`() =
    assertReported(
      "fun foo(role: Role) = Assert(role).isEqualTo(Role.Admin)",
      "Role.Admin" to "Admin",
    )

  @Test
  fun `reports enum entry with explicit type arguments`() =
    assertReported("fun foo() = listOf<Role>(Role.Admin)", "Role.Admin" to "Admin")

  @Test
  fun `reports fully qualified enum entry`() =
    assertReported("fun foo() = takesRole(model.Role.Admin)", "model.Role.Admin" to "Admin")

  @Test
  fun `reports sealed object as an argument`() =
    assertReported("fun foo() = takesState(State.Loading)", "State.Loading" to "Loading")

  @Test
  fun `reports sealed object in a when branch`() =
    assertReported(
      """
      fun foo(state: State) = when (state) {
        State.Loading -> 1
        else -> 2
      }
      """
        .trimIndent(),
      "State.Loading" to "Loading",
    )

  @Test
  fun `reports sealed subtype in a when is-branch`() =
    assertReported(
      """
      fun foo(state: State) = when (state) {
        is State.Failure -> 1
        else -> 2
      }
      """
        .trimIndent(),
      "State.Failure" to "Failure",
    )

  @Test
  fun `reports sealed subtype in an is-check`() =
    assertReported("fun foo(state: State) = state is State.Failure", "State.Failure" to "Failure")

  @Test
  fun `does not report an unqualified entry`() = assertNotReported("fun foo() = takesRole(Admin)")

  @Test
  fun `does not report when there is no expected type`() =
    assertNotReported("val role = Role.Admin", "Role.Admin" to "Admin")

  @Test
  fun `does not report when the argument decides the type`() =
    assertNotReported("fun foo() = listOf(Role.Admin)", "Role.Admin" to "Admin")

  @Test
  fun `does not report when a constructor argument decides the type`() =
    assertNotReported("fun foo() = Assert(Role.Admin)", "Role.Admin" to "Admin")

  @Test
  fun `does not report when the expected type is a supertype`() =
    assertNotReported("fun foo(): Any = Role.Admin", "Role.Admin" to "Admin")

  @Test
  fun `does not report a member access on the entry`() =
    assertNotReported("fun foo(): String = Role.Admin.name", "Role.Admin" to "Admin")

  @Test
  fun `does not report a sealed subtype constructor call`() =
    assertNotReported(
      """fun foo() = takesState(State.Failure("x"))""",
      "State.Failure" to "Failure",
    )

  @Test
  fun `does not report an entry shadowed by a class`() =
    assertNotReported("fun foo() = takesMethod(Method.Password)", "Method.Password" to "Password")

  @Test
  fun `does not report an entry shadowed by a local variable`() =
    assertNotReported(
      """
      fun foo(): Role {
        val Admin = Role.Basic
        return Role.Admin
      }
      """
        .trimIndent()
    )

  @Test
  fun `does not report an entry shadowed by a function`() =
    assertNotReported("fun foo() = takesRole(Role.Basic)\nfun Basic(x: Int) = x")

  @Test
  fun `does not report when overloads disagree on the type`() =
    assertNotReported("fun foo() = overloaded(Role.Admin)", "Role.Admin" to "Admin")

  @Test
  fun `does not report an infix call argument`() =
    assertNotReported("fun foo() = 1 to Role.Admin", "Role.Admin" to "Admin")

  @Test
  fun `does not report an elvis operand`() =
    assertNotReported("fun foo(role: Role?): Role = role ?: Role.Admin")

  @Test
  fun `does not report a lambda result that decides the type`() =
    assertNotReported("fun foo() = listOf(1).associateWith { Role.Admin }", "Role.Admin" to "Admin")

  @Test
  fun `reports a lambda result with a known type`() =
    assertReported(
      """
      fun foo(role: Assert<Role>) = role.map { if (it == makeRole()) Role.Admin else Role.Basic }
      """
        .trimIndent(),
      "Role.Admin" to "Admin",
      "Role.Basic" to "Basic",
    )

  @Test
  fun `does not report a companion property`() =
    assertNotReported("fun foo() = takesState(State.Default)")

  @Test
  fun `does not report when CSR is off`() =
    assertThat(
        lint("$PREAMBLE\nfun foo() = takesRole(Role.Admin)", LanguageVersionSettingsImpl.DEFAULT)
      )
      .isEmpty()

  // Checks the findings, then that the code still compiles once they're applied
  private fun assertReported(@Language("kotlin") code: String, vararg fixes: Pair<String, String>) {
    val source = PREAMBLE + "\n" + code.trimIndent()
    val expected = fixes.map { (from, to) ->
      "'$from' can be written as '$to', the expected type is known"
    }
    assertThat(lint(source).map { it.message }).containsExactly(*expected.toTypedArray())
    compile(fixes.fold(source) { acc, (from, to) -> acc.replace(from, to) })
  }

  // Checks there are no findings, then that the code doesn't compile with the given fixes applied
  private fun assertNotReported(
    @Language("kotlin") code: String,
    vararg fixes: Pair<String, String>,
  ) {
    val source = PREAMBLE + "\n" + code.trimIndent()
    assertThat(lint(source)).isEmpty()
    if (fixes.isEmpty()) return
    val body = fixes.fold(code.trimIndent()) { acc, (from, to) -> acc.replace(from, to) }
    assertFailure { compile(PREAMBLE + "\n" + body) }
  }

  private fun lint(code: String, settings: LanguageVersionSettings = CSR): List<Finding> =
    KotlinAnalysisApiEngine().use { engine ->
      rule.visitFile(engine.compile(code, settings), settings)
    }

  private fun compile(code: String) {
    KotlinAnalysisApiEngine().use { engine -> engine.compile(code, CSR) }
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
    val CSR =
      LanguageVersionSettingsImpl(
        languageVersion = LATEST_STABLE,
        apiVersion = LATEST_STABLE,
        specificFeatures =
          mapOf(
            LanguageFeature.ContextSensitiveResolutionUsingExpectedType to
              LanguageFeature.State.ENABLED
          ),
      )

    @Language("kotlin")
    val PREAMBLE =
      """
      package model

      enum class Role { Admin, Basic }

      enum class Method { Password, Header }

      class Password

      sealed interface State {
        data object Loading : State
        data class Failure(val reason: String) : State
        companion object {
          val Default: State = Loading
        }
      }

      class User(val role: Role)

      class Assert<T>(val actual: T)

      fun <T> Assert<T>.isEqualTo(expected: T) = actual == expected

      fun makeRole(): Role = Role.entries.last()

      fun <T> Assert<T>.map(transform: (T) -> T) = Assert(transform(actual))

      fun takesRole(role: Role) = role

      fun overloaded(role: Role) = role

      fun overloaded(number: Int) = number

      fun takesState(state: State) = state

      fun takesMethod(method: Method) = method
      """
        .trimIndent()
  }
}
