@file:OptIn(KaContextParameterApi::class)

package aktual.detekt.rules

import com.intellij.psi.util.PsiTreeUtil
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.KaContextParameterApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.components.containingDeclaration
import org.jetbrains.kotlin.analysis.api.components.resolveToCall
import org.jetbrains.kotlin.analysis.api.components.resolveToCallCandidates
import org.jetbrains.kotlin.analysis.api.diagnostics.KaDiagnosticWithPsi
import org.jetbrains.kotlin.analysis.api.resolution.KaCallCandidateInfo
import org.jetbrains.kotlin.analysis.api.resolution.KaFunctionCall
import org.jetbrains.kotlin.analysis.api.resolution.KaInapplicableCallCandidateInfo
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaTypeParameterSymbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.analysis.api.types.KaFlexibleType
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.analysis.api.types.KaTypeParameterType
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name.identifier
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.KtValueArgumentList

/**
 * Flags `listOf(a, b)`, `emptyList()`, `setOf(a, b)` and `emptySet()` where a collection literal
 * like `[a, b]` or `[]` means the same thing. Does nothing unless the module compiles with
 * `-Xcollection-literals`.
 *
 * A literal with no expected type, or one that isn't a `Set`, is a `List`, so:
 * - `listOf` and `emptyList` are reported anywhere, unless they have a type argument and nothing
 *   else gives the literal its element type: `val x = listOf<Number>(1)`
 * - `setOf` and `emptySet` are only reported where a `Set` is expected: `val x: Set<Int> =
 *   setOf(1)`, `takesSet(setOf(1))`
 *
 * Not reported:
 * - spread arguments: `listOf(*items)`
 * - arguments of overloaded calls whose candidates expect different types for it, where the literal
 *   would be ambiguous
 */
internal class PreferCollectionLiteral(config: Config) :
  Rule(config = config, description = "Use a collection literal instead"), RequiresAnalysisApi {

  private val enabled: Boolean
    get() = languageVersionSettings.supportsFeature(CollectionLiterals)

  override fun visitCallExpression(expression: KtCallExpression) {
    super.visitCallExpression(expression)
    if (!enabled) return

    val callee = expression.calleeExpression as? KtNameReferenceExpression ?: return
    val name = callee.getReferencedName()
    val kind = FUNCTIONS[name] ?: return
    if (expression.lambdaArguments.isNotEmpty()) return
    if (expression.valueArguments.any { it.getSpreadElement() != null || it.isNamed() }) return

    analyze(expression) {
      val resolved = expression.resolveToCall()?.successfulFunctionCallOrNull() ?: return@analyze
      if (resolved.symbol.callableId != CallableId(COLLECTIONS, identifier(name))) {
        return@analyze
      }

      val expected = if (isReceiver(expression)) null else expression.expectedType?.let(::classIdOf)
      val fits =
        when (kind) {
          Kind.List ->
            expression.typeArgumentList == null || expected != null && expected !in SET_TYPES
          Kind.Set -> expected in SET_TYPES && !isTypeParameterArgument(expression)
        }
      if (!fits || isAmbiguousArgument(expression)) return@analyze
      if (expression.valueArguments.isEmpty() && !hasElementType(expression, expected)) {
        return@analyze
      }

      report(
        Finding(
          entity = Entity.from(expression),
          message = "'$name' can be written as a collection literal",
        ),
      )
    }
  }

  // `[]` has no elements to infer from, so it needs a concrete expected type. `cond(value: Any?)`
  // and `"a" to emptyList()` against `infix fun <A, B> A.to(that: B)` don't give it one, but
  // `isEqualTo(expected: T)` on an `Assert<List<Int>>` does, since T comes from the receiver
  context(_: KaSession)
  private fun hasElementType(expression: KtCallExpression, expected: ClassId?): Boolean {
    if (expected == ANY) return false
    val call = parentCall(expression) ?: return true
    val resolved = call.resolveToCall()?.successfulFunctionCallOrNull() ?: return false
    val function = resolved.symbol
    val declared = resolved.valueArgumentMapping[expression]?.symbol?.returnType ?: return false
    val receiver = function.receiverParameter?.returnType
    return declared.typeParameters().all { parameter ->
      parameter.containingDeclaration != function || receiver?.mentions(parameter) == true
    }
  }

  // `isEqualTo(expected: T)` on an `Assert<Set<Int>>` reports `Set` as the expected type, but the
  // compiler doesn't use T for the literal, so `[1]` would be a `List`
  context(_: KaSession)
  private fun isTypeParameterArgument(expression: KtCallExpression): Boolean {
    val call = parentCall(expression) ?: return false
    val resolved = call.resolveToCall()?.successfulFunctionCallOrNull() ?: return true
    return resolved.valueArgumentMapping[expression]?.symbol?.returnType is KaTypeParameterType
  }

  private fun parentCall(expression: KtExpression): KtElement? =
    when (val parent = expression.parent) {
      is KtValueArgument -> (parent.parent as? KtValueArgumentList)?.parent as? KtElement
      is KtBinaryExpression -> parent
      else -> null
    }

  private fun KaType.typeParameters(): List<KaTypeParameterSymbol> =
    when (this) {
      is KaTypeParameterType -> [symbol]
      is KaClassType -> typeArguments.flatMap { it.type?.typeParameters().orEmpty() }
      else -> []
    }

  private fun KaType.mentions(parameter: KaTypeParameterSymbol): Boolean =
    parameter in typeParameters()

  // `listOf(a).map { }` has no expected type
  private fun isReceiver(call: KtCallExpression): Boolean {
    val parent = call.parent
    return parent is KtDotQualifiedExpression && parent.receiverExpression == call
  }

  // What a parameter of this type turns a literal into, or null if it can't take one. A type
  // parameter, `Any` or a list type all make it a `List`
  private fun literalTarget(type: KaType): ClassId? {
    if (type is KaTypeParameterType) return LIST
    val classId = classIdOf(type)?.takeIf { it in LITERAL_TYPES } ?: return null
    return when {
      classId in SET_TYPES -> SET
      classId.packageFqName == COLLECTIONS || classId == ANY -> LIST
      else -> classId
    }
  }

  private fun classIdOf(type: KaType): ClassId? {
    val lower = (type as? KaFlexibleType)?.lowerBound ?: type
    return (lower as? KaClassType)?.classId
  }

  // `foo(listOf(a))` with `foo(List<A>)` and `foo(Set<A>)` overloads picks a type the literal
  // can't. Overloads that can't take a literal at all, like `plus(other: Pair<A, B>)` next to
  // `plus(other: Iterable<Pair<A, B>>)`, don't count
  context(_: KaSession)
  private fun isAmbiguousArgument(expression: KtExpression): Boolean {
    val (call, argument) =
      when (val parent = expression.parent) {
        is KtValueArgument ->
          ((parent.parent as? KtValueArgumentList)?.parent as? KtCallExpression ?: return false) to
            parent
        // `x + listOf(y)`, with the `plus(element: T)` and `plus(elements: Iterable<T>)` overloads
        is KtBinaryExpression -> parent to expression
        else -> return false
      }
    val types =
      call
        .resolveToCallCandidates()
        .asSequence()
        .filterNot { failsElsewhere(it, argument) }
        .mapNotNull { info ->
          val candidate = info.candidate as? KaFunctionCall<*>
          candidate?.valueArgumentMapping?.get(expression)?.symbol?.returnType?.let(::literalTarget)
        }
        .distinct()
    return types.take(2).count() > 1
  }

  // A candidate that fails on something else, like `Assert<String?>.isEqualTo` called on an
  // `Assert<List<Int>>`, is dropped before the literal is looked at
  private fun failsElsewhere(info: KaCallCandidateInfo, argument: KtElement): Boolean {
    val diagnostic = (info as? KaInapplicableCallCandidateInfo)?.diagnostic
    val psi = (diagnostic as? KaDiagnosticWithPsi<*>)?.psi ?: return false
    return !PsiTreeUtil.isAncestor(argument, psi, false)
  }

  private enum class Kind {
    List,
    Set,
  }

  private companion object {
    val COLLECTIONS = FqName("kotlin.collections")

    val KOTLIN = FqName("kotlin")

    val ANY = ClassId(KOTLIN, identifier("Any"))

    val LIST = ClassId(COLLECTIONS, identifier("List"))

    val SET = ClassId(COLLECTIONS, identifier("Set"))

    val FUNCTIONS =
      mapOf(
        "listOf" to Kind.List,
        "emptyList" to Kind.List,
        "setOf" to Kind.Set,
        "emptySet" to Kind.Set,
      )

    val COLLECTION_NAMES =
      [
        "Collection",
        "Iterable",
        "List",
        "MutableCollection",
        "MutableIterable",
        "MutableList",
        "MutableSet",
        "Set",
      ]

    val KOTLIN_NAMES =
      [
        "Any",
        "Array",
        "BooleanArray",
        "ByteArray",
        "CharArray",
        "DoubleArray",
        "FloatArray",
        "IntArray",
        "LongArray",
        "ShortArray",
      ]

    val LITERAL_TYPES =
      COLLECTION_NAMES.map { ClassId(COLLECTIONS, identifier(it)) }.toSet() +
        KOTLIN_NAMES.map { ClassId(KOTLIN, identifier(it)) }

    val SET_TYPES =
      [
        ClassId(COLLECTIONS, identifier("Set")),
        ClassId(COLLECTIONS, identifier("MutableSet")),
      ]
  }
}
