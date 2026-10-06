package aktual.detekt.rules

import com.intellij.psi.PsiElement
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.KaFunctionCall
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaClassLikeSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaConstructorSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaEnumEntrySymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaTypeParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.markers.KaNamedSymbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.analysis.api.types.KaFunctionType
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.analysis.api.types.KaTypeParameterType
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtContainerNodeForControlStructureBody
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtIsExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.KtUserType
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.KtValueArgumentList
import org.jetbrains.kotlin.psi.KtWhenConditionIsPattern
import org.jetbrains.kotlin.psi.KtWhenConditionWithExpression
import org.jetbrains.kotlin.psi.KtWhenEntry
import org.jetbrains.kotlin.psi.KtWhenExpression
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

/**
 * Flags a qualified enum entry or sealed subtype, like `Role.Admin`, where the expected type is
 * known and context-sensitive resolution (CSR) would resolve a plain `Admin`. Does nothing unless
 * the module compiles with `-Xcontext-sensitive-resolution`.
 *
 * Reported:
 * - function and constructor arguments, named or positional: `takesRole(Role.Admin)`
 * - `when` branches and `==`/`!=`: `when (role) { Role.Admin -> ... }`, `role == Role.Admin`
 * - typed property initialisers, assignments, default parameter values and return values
 * - `is` checks against a sealed subtype: `state is State.Failure`
 * - lambda results where the call already fixes the return type: `flow.update { State.Loading }`
 *
 * Not reported, because the plain name wouldn't compile or would mean something else:
 * - overloads that disagree on the parameter type: `YearMonth(2025, Month.JANUARY)`
 * - names shadowed by another class, property or function in scope, like a `Password` class next to
 *   `LoginMethod.Password`
 * - arguments that decide a generic type themselves: `listOf(Role.Admin)`, `x to Role.Admin`,
 *   `associateWith { Role.Admin }`
 * - elvis operands: `role ?: Role.Admin`
 * - anything with no expected type, or a supertype as the expected type: `val role = Role.Admin`,
 *   `val any: Any = Role.Admin`
 *
 * These checks are conservative, so some qualifiers that could be dropped are missed. Imported
 * entries and companion properties aren't looked at.
 */
internal class RedundantQualifier(config: Config) :
  Rule(
    config = config,
    description = "The qualifier can be dropped, context-sensitive resolution finds the name",
  ),
  RequiresAnalysisApi {

  private val enabled: Boolean
    get() = languageVersionSettings.supportsFeature(ContextSensitiveResolutionUsingExpectedType)

  override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
    super.visitDotQualifiedExpression(expression)
    if (!enabled) return

    val selector = expression.selectorExpression as? KtNameReferenceExpression ?: return
    val parent = expression.parent
    if (parent is KtDotQualifiedExpression && parent.receiverExpression == expression) return
    if (expression.getStrictParentOfType<KtImportDirective>() != null) return
    if (expression.getStrictParentOfType<KtPackageDirective>() != null) return

    analyze(expression) {
      val symbol = selector.mainReference.resolveToSymbol() ?: return@analyze
      val owner = owner(symbol) ?: return@analyze
      val expected = expectedType(expression) ?: return@analyze
      if (!expected.isClass(owner)) return@analyze
      if (isShadowed(expression, symbol)) return@analyze
      report(expression, selector.getReferencedName())
    }
  }

  override fun visitTypeReference(typeReference: KtTypeReference) {
    super.visitTypeReference(typeReference)
    if (!enabled) return

    val userType = typeReference.typeElement as? KtUserType ?: return
    if (userType.qualifier == null) return
    val name = userType.referenceExpression ?: return

    val subject =
      when (val parent = typeReference.parent) {
        is KtIsExpression -> parent.leftHandSide
        is KtWhenConditionIsPattern -> parent.getStrictParentOfType<KtWhenExpression>()
        else -> null
      } ?: return

    analyze(typeReference) {
      val symbol = name.mainReference.resolveToSymbol() ?: return@analyze
      val owner = owner(symbol) ?: return@analyze
      val expected =
        if (subject is KtWhenExpression) subjectType(subject) else subject.expressionType
      if (expected?.isClass(owner) != true) return@analyze
      if (isShadowed(typeReference, symbol)) return@analyze
      report(typeReference, name.getReferencedName())
    }
  }

  private fun report(element: KtElement, name: String) =
    report(
      Finding(
        entity = Entity.from(element),
        message = "'${element.text}' can be written as '$name', the expected type is known",
      )
    )

  // The enum or sealed type whose scope the symbol is found in, if it's one CSR looks in
  private fun KaSession.owner(symbol: KaSymbol): KaNamedClassSymbol? {
    val owner = symbol.containingDeclaration as? KaNamedClassSymbol ?: return null
    return when (symbol) {
      is KaEnumEntrySymbol -> owner
      is KaNamedClassSymbol ->
        owner.takeIf {
          it.modality == SEALED && symbol.classKind != COMPANION_OBJECT && symbol.isSubClassOf(it)
        }
      else -> null
    }
  }

  private fun KaSession.expectedType(expression: KtExpression): KaType? {
    val parent = expression.parent
    val expected =
      when (parent) {
        is KtBinaryExpression if parent.operationToken in EQUALITY -> {
          val other = if (parent.left == expression) parent.right else parent.left
          return other?.expressionType
        }

        is KtWhenConditionWithExpression ->
          return parent.getStrictParentOfType<KtWhenExpression>()?.let { subjectType(it) }

        // Infix calls and elvis don't pass an expected type down the way a plain argument does
        is KtBinaryExpression if parent.operationToken != KtTokens.EQ -> return null

        else -> expression.expectedType ?: return null
      }

    val call = (parent as? KtValueArgument)?.let(::callOf)
    if (call != null) {
      return expected.takeUnless { isInferred(call, expression) { it } }
    }

    val lambda = lambdaReturning(expression) ?: return expected
    val lambdaCall = (lambda.parent as? KtValueArgument)?.let(::callOf) ?: return null
    val inferred = isInferred(lambdaCall, lambda) { (it as? KaFunctionType)?.returnType }
    return expected.takeUnless { inferred }
  }

  private fun KaSession.subjectType(whenExpression: KtWhenExpression): KaType? =
    whenExpression.subjectVariable?.returnType ?: whenExpression.subjectExpression?.expressionType

  private fun callOf(argument: KtValueArgument): KtCallExpression? =
    when (val parent = argument.parent) {
      is KtValueArgumentList -> parent.parent as? KtCallExpression
      else -> parent as? KtCallExpression
    }

  // The lambda whose result this expression is, e.g. the `Role.Admin` in `run { Role.Admin }`
  private fun lambdaReturning(expression: KtExpression): KtLambdaExpression? {
    var element: PsiElement = expression
    while (true) {
      val parent = element.parent ?: return null
      when (parent) {
        is KtBlockExpression -> if (parent.statements.lastOrNull() != element) return null
        is KtIfExpression -> if (parent.condition == element) return null
        is KtWhenEntry,
        is KtWhenExpression,
        is KtContainerNodeForControlStructureBody,
        is KtParenthesizedExpression -> Unit
        is KtReturnExpression -> if (parent.getTargetLabel() == null) return null
        is KtFunctionLiteral -> return parent.parent as? KtLambdaExpression
        else -> return null
      }
      element = parent
    }
  }

  // Whether the type comes from the argument itself, as it does for T in `listOf(Role.Admin)`, or
  // can't be pinned down because overloads disagree about it. CSR has no expected type in both
  private fun KaSession.isInferred(
    call: KtCallExpression,
    argument: KtExpression,
    select: (KaType) -> KaType?,
  ): Boolean {
    val resolved = call.resolveToCall()?.successfulFunctionCallOrNull() ?: return true
    val signature = resolved.valueArgumentMapping[argument] ?: return true
    val type = select(signature.symbol.returnType) ?: return true
    val substituted = select(signature.returnType) ?: return true

    val overloadsDisagree =
      call.resolveToCallCandidates().any { info ->
        val candidate = info.candidate as? KaFunctionCall<*> ?: return@any false
        val other = candidate.valueArgumentMapping[argument]?.returnType ?: return@any false
        select(other)?.semanticallyEquals(substituted) != true
      }
    if (overloadsDisagree) return true

    if (type !is KaTypeParameterType || call.typeArgumentList != null) return false
    val function = resolved.symbol
    if (function is KaConstructorSymbol) return true
    if (type.symbol.containingDeclaration != function) return false
    // `Assert<T>.isEqualTo(expected: T)` gets T from its receiver, not the argument
    return function.receiverParameter?.returnType?.mentions(type.symbol) != true
  }

  private fun KaType.mentions(parameter: KaTypeParameterSymbol): Boolean =
    when (this) {
      is KaTypeParameterType -> symbol == parameter
      is KaClassType -> typeArguments.any { it.type?.mentions(parameter) == true }
      else -> false
    }

  private fun KaType.isClass(symbol: KaClassLikeSymbol): Boolean =
    (this as? KaClassType)?.symbol == symbol

  // A plain name that already means something else here wins over CSR
  private fun KaSession.isShadowed(position: KtElement, target: KaSymbol): Boolean {
    val name = (target as? KaNamedSymbol)?.name ?: return false
    return position.containingKtFile.scopeContext(position).scopes.any { scoped ->
      scoped.scope.classifiers(name).any { it != target } ||
        scoped.scope.callables(name).any { it != target }
    }
  }

  private companion object {
    val EQUALITY = setOf(KtTokens.EQEQ, KtTokens.EXCLEQ)
  }
}
