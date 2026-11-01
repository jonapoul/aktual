package aktual.detekt.rules

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.diagnostics.KaDiagnosticWithPsi
import org.jetbrains.kotlin.analysis.api.resolution.KaCallCandidateInfo
import org.jetbrains.kotlin.analysis.api.resolution.KaFunctionCall
import org.jetbrains.kotlin.analysis.api.resolution.KaInapplicableCallCandidateInfo
import org.jetbrains.kotlin.analysis.api.resolution.successfulFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaClassLikeSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaConstructorSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaEnumEntrySymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaPropertySymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaTypeParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.markers.KaNamedSymbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.analysis.api.types.KaFlexibleType
import org.jetbrains.kotlin.analysis.api.types.KaFunctionType
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.analysis.api.types.KaTypeParameterType
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtArrayAccessExpression
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBinaryExpressionWithTypeRHS
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtCatchClause
import org.jetbrains.kotlin.psi.KtContainerNode
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
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.KtTryExpression
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.KtUserType
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.KtValueArgumentList
import org.jetbrains.kotlin.psi.KtWhenConditionIsPattern
import org.jetbrains.kotlin.psi.KtWhenConditionWithExpression
import org.jetbrains.kotlin.psi.KtWhenEntry
import org.jetbrains.kotlin.psi.KtWhenExpression
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * Flags a qualified enum entry, sealed subtype or companion property, like `Role.Admin`, where the
 * expected type is known and context-sensitive resolution (CSR) would resolve a plain `Admin`. Does
 * nothing unless the module compiles with `-Xcontext-sensitive-resolution`.
 *
 * Reported:
 * - function and constructor arguments, named or positional: `takesRole(Role.Admin)`
 * - `when` branches and equality checks: `when (role) { Role.Admin -> ... }`, `role == Role.Admin`
 * - operator and infix arguments: `amount > Amount.Zero`, `Role.Admin in roles`
 * - typed property initialisers, assignments, default parameter values and return values
 * - `is` checks and `as`/`as?` casts to a sealed subtype: `state is State.Failure`, `state as
 *   State.Failure`
 * - lambda results where the call already fixes the return type: `flow.update { State.Loading }`
 * - lambda results with a declared function type: `val f: () -> Role = { Role.Admin }`
 * - `try`/`catch` results with a known type
 * - the right side of an elvis: `role ?: Role.Admin`
 * - properties in the companion of the expected type: `clickable(role = Role.Button)`
 * - two or more arguments of one type that decide a generic type, as a type argument would give
 *   them an expected type: `listOf(Role.Admin, Role.Basic)` can be `listOf<Role>(Admin, Basic)`
 * - arguments that decide a generic type the call's own expected type already fixes:
 *   `takesRoles(setOf(Role.Admin))` against `takesRoles(roles: Set<Role>)`
 *
 * Not reported, because the plain name wouldn't compile or would mean something else:
 * - overloads that disagree on the parameter type: `YearMonth(2025, Month.JANUARY)`
 * - names shadowed by another class, property or function in scope, like a `Password` class next to
 *   `LoginMethod.Password`, or a `Button` composable next to `Role.Button`
 * - other arguments that decide a generic type themselves: `listOf(Role.Admin)`, `x to Role.Admin`,
 *   `associateWith { Role.Admin }`, `prefs[Key.Name]` against `get(key: Key<T>)`
 * - sealed subtypes and companion properties in an `if`/`when` passed as an argument, where another
 *   branch can narrow the expected type: `update(if (x) Patch.To(1) else Patch.Keep)`
 * - anything with no expected type, or a supertype as the expected type: `val role = Role.Admin`,
 *   `val any: Any = Role.Admin`, `override val role = Role.Admin`, `val role = if (x) Role.Admin
 *   else Role.Basic`
 * - compound assignments: `roles += Role.Admin`
 * - SAM constructor lambdas and `by lazy {}` initialisers: `RoleSource { Role.Admin }`
 *
 * These checks are conservative, so some qualifiers that could be dropped are missed. Imports of
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
      if (symbol !is KaEnumEntrySymbol && isArgumentBranch(expression)) return@analyze
      if (isShadowed(expression, symbol)) return@analyze
      report(expression, selector.getReferencedName(), "the expected type is known")
    }
  }

  override fun visitCallExpression(expression: KtCallExpression) {
    super.visitCallExpression(expression)
    if (!enabled || expression.typeArgumentList != null) return

    val callee = expression.calleeExpression as? KtNameReferenceExpression ?: return
    val qualified = qualifiedArguments(expression) ?: return
    val names = qualified.map { it.selectorExpression as? KtNameReferenceExpression ?: return }
    val receiver = qualified.map { it.receiverExpression.text }.distinct().singleOrNull() ?: return

    analyze(expression) {
      val resolved = expression.resolveToCall()?.successfulFunctionCallOrNull() ?: return@analyze
      if (resolved.typeArgumentsMapping.size != 1) return@analyze

      val owner =
        names
          .map { name ->
            val symbol = name.mainReference.resolveToSymbol() ?: return@analyze
            if (isShadowed(name, symbol)) return@analyze
            owner(symbol) ?: return@analyze
          }
          .distinct()
          .singleOrNull() ?: return@analyze
      if (!qualifiesWith(qualified.first(), owner)) return@analyze

      val decided = qualified.all { argument ->
        val signature = resolved.valueArgumentMapping[argument] ?: return@all false
        val declared = signature.symbol.returnType
        declared is KaTypeParameterType &&
          isDecidedByArgument(declared.symbol, resolved.symbol) &&
          signature.returnType.isClass(owner)
      }
      if (!decided) return@analyze
      // Each name is reported on its own instead
      if (typeFromCallContext(expression, qualified.first()) != null) return@analyze

      val replacement =
        "${callee.text}<$receiver>(${names.joinToString { it.getReferencedName() }})"
      report(expression, replacement, "a type argument gives the names an expected type")
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
        is KtBinaryExpressionWithTypeRHS -> parent.left
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
      report(typeReference, name.getReferencedName(), "the expected type is known")
    }
  }

  // Every argument of the call, if there are at least two and all are plain `Type.Name` ones
  private fun qualifiedArguments(call: KtCallExpression): List<KtDotQualifiedExpression>? {
    val arguments = call.valueArguments
    if (arguments.size < 2) return null
    if (arguments.any { it.isNamed() || it.getSpreadElement() != null }) return null
    return arguments.map { it.getArgumentExpression() as? KtDotQualifiedExpression ?: return null }
  }

  private fun report(element: KtElement, replacement: String, reason: String) =
    report(
      Finding(
        entity = Entity.from(element),
        message = "'${element.text}' can be written as '$replacement', $reason",
      ),
    )

  // Whether the qualifier names the class itself, like `Role` in `Role.Admin` or `model.Role` in
  // `model.Role.Admin`. In `Kind.Button` the `Kind` resolves to the companion, not the class
  private fun KaSession.qualifiesWith(
    expression: KtDotQualifiedExpression,
    owner: KaNamedClassSymbol,
  ): Boolean {
    val receiver = expression.receiverExpression
    val name =
      ((receiver as? KtDotQualifiedExpression)?.selectorExpression ?: receiver)
        as? KtNameReferenceExpression ?: return false
    if (name.getReferencedName() != owner.name.asString()) return false
    val symbol = name.mainReference.resolveToSymbol()
    return symbol == owner ||
      symbol is KaNamedClassSymbol &&
        symbol.classKind == COMPANION_OBJECT &&
        symbol.containingDeclaration == owner
  }

  // The type whose scope the symbol is found in, if it's one CSR looks in
  private fun KaSession.owner(symbol: KaSymbol): KaNamedClassSymbol? {
    val owner = symbol.containingDeclaration as? KaNamedClassSymbol ?: return null
    return when (symbol) {
      is KaEnumEntrySymbol -> owner
      is KaNamedClassSymbol ->
        owner.takeIf {
          it.modality == SEALED && symbol.classKind != COMPANION_OBJECT && symbol.isSubClassOf(it)
        }
      is KaPropertySymbol ->
        (owner.containingDeclaration as? KaNamedClassSymbol)?.takeIf {
          owner.classKind == COMPANION_OBJECT && !symbol.isExtension
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
      is KtContainerNode if parent.parent is KtArrayAccessExpression ->
        indexType(parent, expression)
      is KtParenthesizedExpression -> expectedType(parent)
      else ->
        tryResultType(expression)
          ?: expression.expectedType?.takeUnless {
            isUntypedDeclaration(expression) || isInferredLambdaResult(expression)
          }
    }

  // `override val role = Role.Admin` and `val role = if (x) Role.Admin else Role.Basic` take their
  // type from the initialiser, so there is no expected type for CSR to use
  private fun isUntypedDeclaration(expression: KtExpression): Boolean {
    val parent =
      expression.parents.firstOrNull {
        !it.isBranchBody() && it !is KtBlockExpression && it !is KtParenthesizedExpression
      }
    val declaration = (parent as? KtPropertyAccessor)?.property ?: parent
    return declaration is KtCallableDeclaration &&
      declaration !is KtFunctionLiteral &&
      declaration.typeReference == null
  }

  private fun KaSession.argumentType(argument: KtValueArgument, expression: KtExpression): KaType? {
    val call = callOf(argument) ?: return expression.expectedType
    return if (isInferred(call, expression) { type -> type }) {
      typeFromCallContext(call, expression)
    } else {
      expression.expectedType
    }
  }

  // `takesRoles(setOf(Role.Admin))` against `takesRoles(roles: Set<Role>)` gets T from the type
  // the call itself is expected to have
  private fun KaSession.typeFromCallContext(
    call: KtCallExpression,
    argument: KtExpression,
  ): KaType? {
    if (call.typeArgumentList != null) return null
    val resolved = call.resolveToCall()?.successfulFunctionCallOrNull() ?: return null
    val declared = resolved.valueArgumentMapping[argument]?.symbol?.returnType
    if (declared !is KaTypeParameterType) return null
    val parent = call.parent
    val whole =
      if (parent is KtDotQualifiedExpression && parent.selectorExpression == call) parent else call
    val outer = expectedType(whole) ?: return null
    return typeArgumentFor(resolved.symbol.returnType, declared.symbol, outer)
  }

  // The type `outer` gives the parameter, like `Role` for T when `Set<T>` is expected as
  // `Set<Role>`
  private fun KaSession.typeArgumentFor(
    declared: KaType,
    parameter: KaTypeParameterSymbol,
    outer: KaType,
  ): KaType? {
    if (declared is KaTypeParameterType) return outer.takeIf { declared.symbol == parameter }
    val outerClass =
      ((outer as? KaFlexibleType)?.lowerBound ?: outer) as? KaClassType ?: return null
    val match =
      (sequenceOf(declared) + declared.allSupertypes).filterIsInstance<KaClassType>().firstOrNull {
        it.symbol == outerClass.symbol
      } ?: return null
    val index =
      match.typeArguments.indexOfFirst { (it.type as? KaTypeParameterType)?.symbol == parameter }
    return outerClass.typeArguments.getOrNull(index)?.type
  }

  // `prefs[Key.Name]` against `operator fun <T> get(key: Key<T>)` decides T from the index
  private fun KaSession.indexType(node: KtContainerNode, expression: KtExpression): KaType? {
    val access = node.parent as? KtArrayAccessExpression ?: return null
    if (expression !in access.indexExpressions) return null
    return if (isInferred(access, expression) { type -> type }) null else expression.expectedType
  }

  private fun KaSession.operandType(binary: KtBinaryExpression, operand: KtExpression): KaType? =
    when (binary.operationToken) {
      in EQUALITY -> (if (binary.left == operand) binary.right else binary.left)?.expressionType
      KtTokens.EQ -> operand.expectedType
      // The right side of `role ?: Role.Admin` takes its expected type from the left
      KtTokens.ELVIS -> if (binary.right == operand) binary.left?.expressionType else null
      // `roles += Role.Admin` doesn't pass an expected type down
      in KtTokens.AUGMENTED_ASSIGNMENTS -> null
      else -> callOperandType(binary, operand)
    }

  // Operators and infix calls, like `amount > Amount.Zero` or `Role.Admin in roles`
  private fun KaSession.callOperandType(
    binary: KtBinaryExpression,
    operand: KtExpression,
  ): KaType? {
    val resolved = binary.resolveToCall()?.successfulFunctionCallOrNull()
    val signature = resolved?.valueArgumentMapping?.get(operand)
    return signature?.returnType?.takeUnless { isInferred(binary, operand) { type -> type } }
  }

  // The result of a `try` or `catch` block takes the expected type of the whole `try`
  private fun KaSession.tryResultType(expression: KtExpression): KaType? {
    val block = expression.parent as? KtBlockExpression ?: return null
    if (block.statements.lastOrNull() != expression) return null
    val tryExpression =
      when (val parent = block.parent) {
        is KtTryExpression -> parent.takeIf { it.tryBlock == block }
        is KtCatchClause -> parent.parent as? KtTryExpression
        else -> null
      }
    return tryExpression?.let { expectedType(it) }
  }

  // In `update(if (x) Patch.To(1) else Keep)` the other branch narrows the expected type to
  // `Patch.To<Int>` and `Keep` no longer fits. Enum entries all share one type, so they are fine
  private fun isArgumentBranch(expression: KtExpression): Boolean {
    val path =
      expression.parents
        .takeWhile {
          it.isBranchBody() || it is KtBlockExpression || it is KtParenthesizedExpression
        }
        .toList()
    return path.any { it.isBranchBody() } && path.last().parent is KtValueArgument
  }

  private fun PsiElement.isBranchBody(): Boolean =
    this is KtContainerNodeForControlStructureBody ||
      this is KtWhenEntry ||
      this is KtIfExpression ||
      this is KtWhenExpression ||
      this is KtTryExpression ||
      this is KtCatchClause

  private fun KaSession.subjectType(whenExpression: KtWhenExpression): KaType? =
    whenExpression.subjectVariable?.returnType ?: whenExpression.subjectExpression?.expressionType

  private fun callOf(argument: KtValueArgument): KtCallExpression? =
    when (val parent = argument.parent) {
      is KtValueArgumentList -> parent.parent as? KtCallExpression
      else -> parent as? KtCallExpression
    }

  private fun KaSession.isInferredLambdaResult(expression: KtExpression): Boolean {
    val lambda = lambdaReturning(expression) ?: return false
    // `val f: () -> Role = { Role.Admin }` takes the result type from the declaration
    val argument = lambda.parent as? KtValueArgument ?: return isUntypedDeclaration(lambda)
    val call = callOf(argument) ?: return true
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
    return if (passesResultUp) lambdaReturning(parent) else null
  }

  // Whether the type comes from the argument itself, as it does for T in `listOf(Role.Admin)` or in
  // `get(key: Key<T>)`, or can't be pinned down because overloads disagree about it. CSR has no
  // expected type in both
  private fun KaSession.isInferred(
    call: KtElement,
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
        other != null &&
          !failsElsewhere(info, argument) &&
          select(other)?.semanticallyEquals(substituted) != true
      }

    return overloadsDisagree ||
      (call as? KtCallExpression)?.typeArgumentList == null &&
        declared.typeParameters().any { isDecidedByArgument(it, resolved.symbol) }
  }

  // A candidate that fails on something other than this argument, like `Assert<String?>.isEqualTo`
  // called on an `Assert<Role>`, is dropped before CSR looks at the argument
  private fun failsElsewhere(info: KaCallCandidateInfo, argument: KtExpression): Boolean {
    val diagnostic = (info as? KaInapplicableCallCandidateInfo)?.diagnostic
    val psi = (diagnostic as? KaDiagnosticWithPsi<*>)?.psi ?: return false
    return !PsiTreeUtil.isAncestor(argument.parent as? KtValueArgument ?: argument, psi, false)
  }

  // `Assert<T>.isEqualTo(expected: T)` gets T from its receiver, not the argument
  private fun KaSession.isDecidedByArgument(
    parameter: KaTypeParameterSymbol,
    function: KaFunctionSymbol,
  ): Boolean =
    function is KaConstructorSymbol ||
      parameter.containingDeclaration == function &&
        function.receiverParameter?.returnType?.mentions(parameter) != true

  private fun KaType.typeParameters(): List<KaTypeParameterSymbol> =
    when (this) {
      is KaTypeParameterType -> listOf(symbol)
      is KaClassType -> typeArguments.flatMap { it.type?.typeParameters().orEmpty() }
      else -> emptyList()
    }

  private fun KaType.mentions(parameter: KaTypeParameterSymbol): Boolean =
    when (this) {
      is KaTypeParameterType -> symbol == parameter
      is KaClassType -> typeArguments.any { it.type?.mentions(parameter) == true }
      else -> false
    }

  // Java parameters have platform types like `TimeUnit!`
  private fun KaType.isClass(symbol: KaClassLikeSymbol): Boolean {
    val type = (this as? KaFlexibleType)?.lowerBound ?: this
    return (type as? KaClassType)?.symbol == symbol
  }

  // A plain name that already means something else here wins over CSR
  private fun KaSession.isShadowed(position: KtElement, target: KaSymbol): Boolean {
    val name = (target as? KaNamedSymbol)?.name ?: return false
    return position.containingKtFile.scopeContext(position).scopes.any { scoped ->
      scoped.scope.classifiers(name).any { it != target } ||
        scoped.scope.callables(name).any { it != target }
    }
  }

  private companion object {
    val EQUALITY = setOf(KtTokens.EQEQ, KtTokens.EXCLEQ, KtTokens.EQEQEQ, KtTokens.EXCLEQEQEQ)
  }
}
