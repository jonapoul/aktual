package aktual.compiler.id

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind.Common
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirClassChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors.OTHER_ERROR_WITH_REASON
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider

// Reports a compile error for @Id classes whose wrapped value type isn't Comparable, since
// IdFirSupertypeGenerationExtension and IdFirDeclarationGenerationExtension silently skip
// generating
// Comparable/compareTo() in that case.
internal object IdComparableChecker : FirClassChecker(Common) {
  context(context: CheckerContext, reporter: DiagnosticReporter)
  override fun check(declaration: FirClass) {
    if (declaration !is FirRegularClass) return
    if (!context.session.predicateBasedProvider.matches(IdPredicate, declaration.symbol)) return

    val valueType = declaration.symbol.resolvedValuePropertyType() ?: return
    if (valueType.isComparableToSelf(context.session)) return

    reporter.reportOn(
      source = declaration.source,
      factory = OTHER_ERROR_WITH_REASON,
      a =
        "@Id class '${declaration.name}' wraps a type that isn't Comparable, so toString()/compareTo() can't be generated",
    )
  }
}
