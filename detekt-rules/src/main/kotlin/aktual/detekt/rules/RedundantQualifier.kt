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
import org.jetbrains.kotlin.analysis.api.symbols.KaFunctionSymbol
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

  private fun KaSession.expectedType(expression: KtExpression): KaType? =
    when (val parent = expression.parent) {
      is KtBinaryExpression -> operandType(parent, expression)
      is KtWhenConditionWithExpression ->
        parent.getStrictParentOfType<KtWhenExpression>()?.let { subjectType(it) }
      is KtValueArgument -> argumentType(parent, expression)
      else -> expression.expectedType?.takeUnless { isInferredLambdaResult(expression) }
    }

  private fun KaSession.argumentType(argument: KtValueArgument, expression: KtExpression): KaType? {
    val call = callOf(argument)
    val inferred = call != null && isInferred(call, expression) { type -> type }
    return if (inferred) null else expression.expectedType
  }

  private fun KaSession.operandType(binary: KtBinaryExpression, operand: KtExpression): KaType? =
    when (binary.operationToken) {
      in EQUALITY -> (if (binary.left == operand) binary.right else binary.left)?.expressionType
      KtTokens.EQ -> operand.expectedType
      // Infix calls and elvis don't pass an expected type down the way a plain argument does
      else -> null
    }

  private fun KaSession.subjectType(whenExpression: KtWhenExpression): KaType? =
    whenExpression.subjectVariable?.returnType ?: whenExpression.subjectExpression?.expressionType

  private fun callOf(argument: KtValueArgument): KtCallExpression? =
    when (val parent = argument.parent) {
      is KtValueArgumentList -> parent.parent as? KtCallExpression
      else -> parent as? KtCallExpression
    }

  private fun KaSession.isInferredLambdaResult(expression: KtExpression): Boolean {
    val lambda = lambdaReturning(expression) ?: return false
    val call = (lambda.parent as? KtValueArgument)?.let(::callOf) ?: return true
    return isInferred(call, lambda) { type -> (type as? KaFunctionType)?.returnType }
  }

  // The lambda whose result this element is, e.g. the `Role.Admin` in `run { Role.Admin }`
  private fun lambdaReturning(element: PsiElement): KtLambdaExpression? {
    val parent = element.parent
    val passesResultUp =
      when (parent) {
        is KtFunctionLiteral -> return parent.parent as? KtLambdaExpression
        is KtBlockExpression -> parent.statements.lastOrNull() == element
        is KtIfExpression -> parent.condition != element
        is KtReturnExpression -> parent.getTargetLabel() != null
        is KtWhenEntry,
        is KtWhenExpression,
        is KtContainerNodeForControlStructureBody,
        is KtParenthesizedExpression -> true
        else -> false
      }
    return if (passesResultUp && parent != null) lambdaReturning(parent) else null
  }

  // Whether the type comes from the argument itself, as it does for T in `listOf(Role.Admin)`, or
  // can't be pinned down because overloads disagree about it. CSR has no expected type in both
  private fun KaSession.isInferred(
    call: KtCallExpression,
    argument: KtExpression,
    select: (KaType) -> KaType?,
  ): Boolean {
    val resolved = call.resolveToCall()?.successfulFunctionCallOrNull()
    val signature = resolved?.valueArgumentMapping?.get(argument)
    val declared = signature?.let { select(it.symbol.returnType) }
    val substituted = signature?.let { select(it.returnType) }
    if (resolved == null || declared == null || substituted == null) return true

    val overloadsDisagree =
      call.resolveToCallCandidates().any { info ->
        val candidate = info.candidate as? KaFunctionCall<*>
        val other = candidate?.valueArgumentMapping?.get(argument)?.returnType
        other != null && select(other)?.semanticallyEquals(substituted) != true
      }

    return overloadsDisagree ||
      call.typeArgumentList == null &&
        declared is KaTypeParameterType &&
        isDecidedByArgument(declared.symbol, resolved.symbol)
  }

  // `Assert<T>.isEqualTo(expected: T)` gets T from its receiver, not the argument
  private fun KaSession.isDecidedByArgument(
    parameter: KaTypeParameterSymbol,
    function: KaFunctionSymbol,
  ): Boolean =
    function is KaConstructorSymbol ||
      parameter.containingDeclaration == function &&
        function.receiverParameter?.returnType?.mentions(parameter) != true

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
