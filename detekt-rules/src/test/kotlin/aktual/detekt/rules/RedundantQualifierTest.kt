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
  fun `reports sealed object in an assertion on a property`() =
    assertReported(
      "fun foo(screen: Assert<Screen>) = screen.prop(Screen::state).isEqualTo(State.Loading)",
      "State.Loading" to "Loading",
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
  fun `reports sealed subtype in a cast`() =
    assertReported("fun foo(state: State) = state as State.Failure", "State.Failure" to "Failure")

  @Test
  fun `reports sealed subtype in a safe cast`() =
    assertReported("fun foo(state: State) = state as? State.Failure", "State.Failure" to "Failure")

  @Test
  fun `does not report a cast from a supertype`() =
    assertNotReported("fun foo(state: Any) = state as State.Failure", "State.Failure" to "Failure")

  @Test
  fun `does not report an unqualified entry`() = assertNotReported("fun foo() = takesRole(Admin)")

  @Test
  fun `does not report when there is no expected type`() =
    assertNotReported("val role = Role.Admin", "Role.Admin" to "Admin")

  @Test
  fun `does not report an override with no declared type`() =
    assertNotReported(
      """
      class Impl : HasRole {
        override val role = Role.Admin
        override fun other() = Role.Basic
      }
      """
        .trimIndent(),
      "Role.Admin" to "Admin",
    )

  @Test
  fun `does not report an override getter with no declared type`() =
    assertNotReported(
      """
      class Impl : HasRole {
        override val role get() = Role.Admin
        override fun other() = makeRole()
      }
      """
        .trimIndent(),
      "Role.Admin" to "Admin",
    )

  @Test
  fun `does not report branches of a property with no declared type`() =
    assertNotReported(
      """
      fun foo(set: Boolean): Role {
        val role = if (set) Role.Admin else Role.Basic
        val kind = when (role) {
          Admin -> Kind.Button
          Basic -> Kind.Checkbox
        }
        return if (kind == Button) role else makeRole()
      }
      """
        .trimIndent(),
      "Role.Admin" to "Admin",
      "Role.Basic" to "Basic",
    )

  @Test
  fun `reports an override with a declared type`() =
    assertReported(
      """
      class Impl : HasRole {
        override val role: Role = Role.Admin
        override fun other(): Role = makeRole()
      }
      """
        .trimIndent(),
      "Role.Admin" to "Admin",
    )

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
        .trimIndent(),
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
  fun `reports an elvis operand with a known type`() =
    assertReported("fun foo(role: Role?): Role = role ?: Role.Admin", "Role.Admin" to "Admin")

  @Test
  fun `reports an elvis operand as an argument`() =
    assertReported("fun foo(role: Role?) = takesRole(role ?: Role.Admin)", "Role.Admin" to "Admin")

  @Test
  fun `reports sealed object as an elvis operand`() =
    assertReported(
      "fun foo(patch: Patch<Int>?) = takesPatch(patch ?: Patch.Keep)",
      "Patch.Keep" to "Keep",
    )

  @Test
  fun `reports companion property as an elvis operand`() =
    assertReported(
      "fun foo(kind: Kind?) = takesKind(kind ?: Kind.Button)",
      "Kind.Button" to "Button",
    )

  @Test
  fun `reports an elvis operand with no expected type`() =
    assertReported("fun foo(role: Role?) = role ?: Role.Admin", "Role.Admin" to "Admin")

  @Test
  fun `reports an elvis operand in an argument that decides the type`() =
    assertReported("fun foo(role: Role?) = listOf(role ?: Role.Admin)", "Role.Admin" to "Admin")

  @Test
  fun `does not report an elvis operand of another type`() =
    assertNotReported("fun foo(any: Any?) = any ?: Role.Admin", "Role.Admin" to "Admin")

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
  fun `reports generic sealed object as an argument`() =
    assertReported("fun foo() = takesPatch(Patch.Keep)", "Patch.Keep" to "Keep")

  @Test
  fun `does not report sealed object in an argument branch`() =
    assertNotReported(
      "fun foo(set: Boolean) = takesPatch(if (set) Patch.To(1) else Patch.Keep)",
      "Patch.Keep" to "Keep",
    )

  @Test
  fun `reports sealed object in a return branch`() =
    assertReported(
      "fun foo(set: Boolean): Patch<Int> = if (set) Patch.To(1) else Patch.Keep",
      "Patch.Keep" to "Keep",
    )

  @Test
  fun `reports enum entry in an argument branch`() =
    assertReported(
      "fun foo(set: Boolean) = takesRole(if (set) Role.Admin else Role.Basic)",
      "Role.Admin" to "Admin",
      "Role.Basic" to "Basic",
    )

  @Test
  fun `reports companion property as an argument`() =
    assertReported("fun foo() = takesState(State.Default)", "State.Default" to "Default")

  @Test
  fun `reports companion property of a value class as a named argument`() =
    assertReported("fun foo() = takesKind(kind = Kind.Button)", "Kind.Button" to "Button")

  @Test
  fun `reports companion property in an equality check`() =
    assertReported("fun foo(kind: Kind) = kind == Kind.Button", "Kind.Button" to "Button")

  @Test
  fun `reports companion property as a default parameter value`() =
    assertReported("fun foo(kind: Kind? = Kind.Button) = kind", "Kind.Button" to "Button")

  @Test
  fun `does not report a companion property shadowed by a function`() =
    assertNotReported("fun foo() = takesKind(Kind.Button)\nfun Button(x: Int) = x")

  @Test
  fun `does not report a companion property of another type`() =
    assertNotReported("fun foo() = takesRole(Kind.Fallback)", "Kind.Fallback" to "Fallback")

  @Test
  fun `does not report a companion property in an argument branch`() =
    assertNotReported("fun foo(set: Boolean) = takesKind(if (set) Kind.Button else Kind.Checkbox)")

  @Test
  fun `reports enum entries that decide a collection's type`() =
    assertReported(
      "fun foo() = listOf(Role.Admin, Role.Basic)",
      "listOf(Role.Admin, Role.Basic)" to "listOf<Role>(Admin, Basic)",
    )

  @Test
  fun `reports sealed objects that decide a collection's type`() =
    assertReported(
      "fun foo() = setOf(State.Loading, State.Idle)",
      "setOf(State.Loading, State.Idle)" to "setOf<State>(Loading, Idle)",
    )

  @Test
  fun `reports companion properties that decide a collection's type`() =
    assertReported(
      "fun foo() = setOf(Kind.Button, Kind.Checkbox)",
      "setOf(Kind.Button, Kind.Checkbox)" to "setOf<Kind>(Button, Checkbox)",
    )

  @Test
  fun `reports enum entries split over lines`() =
    assertReported(
      """
      fun foo() =
        setOf(
          Role.Admin,
          Role.Basic,
        )
      """
        .trimIndent(),
      "setOf(\n    Role.Admin,\n    Role.Basic,\n  )" to "setOf<Role>(Admin, Basic)",
    )

  @Test
  fun `reports enum entries that decide a generic function's type`() =
    assertReported(
      "fun foo() = both(Role.Admin, Role.Basic)",
      "both(Role.Admin, Role.Basic)" to "both<Role>(Admin, Basic)",
    )

  @Test
  fun `does not report entries of different types`() =
    assertNotReported("fun foo() = listOf(Role.Admin, Method.Header)")

  @Test
  fun `does not report when one argument is unqualified`() =
    assertNotReported("fun foo() = listOf(Role.Admin, makeRole())")

  @Test
  fun `does not report when one argument is a constructor call`() =
    assertNotReported("""fun foo() = listOf(State.Loading, State.Failure("x"))""")

  @Test
  fun `does not report when one entry is shadowed`() =
    assertNotReported(
      "fun foo() = listOf(Method.Password, Method.Header)",
      "listOf(Method.Password, Method.Header)" to "listOf<Method>(Password, Header)",
    )

  @Test
  fun `does not report when the expected type widens the collection`() =
    assertNotReported("val roles: MutableList<Any> = mutableListOf(Role.Admin, Role.Basic)")

  @Test
  fun `does not report a function with more than one type parameter`() =
    assertNotReported("fun foo() = pairOf(Role.Admin, Role.Basic)")

  @Test
  fun `does not report when CSR is off`() =
    assertThat(
        lint("$PREAMBLE\nfun foo() = takesRole(Role.Admin)", LanguageVersionSettingsImpl.DEFAULT),
      )
      .isEmpty()

  // Checks the findings, then that the code still compiles once they're applied
  private fun assertReported(@Language("kotlin") code: String, vararg fixes: Pair<String, String>) {
    val source = PREAMBLE + "\n" + code.trimIndent()
    val expected = fixes.map { (from, to) -> "'$from' can be written as '$to'" }
    val messages = lint(source).map { it.message.substringBeforeLast("', ") + "'" }
    assertThat(messages).containsExactly(*expected.toTypedArray())
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
              LanguageFeature.State.ENABLED,
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
        data object Idle : State
        data class Failure(val reason: String) : State
        companion object {
          val Default: State = Loading
        }
      }

      @JvmInline
      value class Kind(val value: Int) {
        companion object {
          val Button = Kind(0)
          val Checkbox = Kind(1)
          val Fallback: Role = makeRole()
        }
      }

      fun takesKind(kind: Kind?) = kind

      sealed interface Patch<out T> {
        data object Keep : Patch<Nothing>
        data class To<out T>(val value: T) : Patch<T>
      }

      fun takesPatch(patch: Patch<Int>) = patch

      class User(val role: Role)

      interface HasRole {
        val role: Role
        fun other(): Role
      }

      class Screen(val state: State)

      class Assert<out T>(val actual: T)

      fun <T> Assert<T>.isEqualTo(expected: T) = actual == expected

      fun Assert<String?>.isEqualTo(other: String?, ignoreCase: Boolean = false) =
        actual.equals(other, ignoreCase)

      fun <T> Assert<Array<T>>.isEqualTo(expected: Array<T>) = actual.contentEquals(expected)

      fun <T, P> Assert<T>.prop(property: kotlin.reflect.KProperty1<T, P>) = Assert(property.get(actual))

      fun makeRole(): Role = Role.entries.last()

      fun <T> Assert<T>.map(transform: (T) -> T) = Assert(transform(actual))

      fun takesRole(role: Role) = role

      fun overloaded(role: Role) = role

      fun overloaded(number: Int) = number

      fun takesState(state: State) = state

      fun takesMethod(method: Method) = method

      fun <T> both(first: T, second: T) = first to second

      fun <A, B> pairOf(first: A, second: B) = first to second
      """
        .trimIndent()
  }
}
