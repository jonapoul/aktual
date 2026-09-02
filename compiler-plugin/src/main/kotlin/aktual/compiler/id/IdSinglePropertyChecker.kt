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

// Reports a compile error for @IdType classes that don't have exactly one declared property.
// IdFirDeclarationGenerationExtension generates toString()/compareTo() unconditionally (property
// types aren't resolved yet at that stage), and IdIrGenerationExtension assumes there's exactly one
// property to wrap those bodies around - without this check, a class with zero or multiple
// properties would crash the IR pass with an unhandled exception instead of a clean diagnostic.
internal object IdSinglePropertyChecker : FirClassChecker(Common) {
  context(context: CheckerContext, reporter: DiagnosticReporter)
  override fun check(declaration: FirClass) {
    if (declaration !is FirRegularClass) return
    if (!context.session.predicateBasedProvider.matches(IdPredicate, declaration.symbol)) return

    val propertyCount = declaration.symbol.valueProperties().size
    if (propertyCount == 1) return

    reporter.reportOn(
      source = declaration.source,
      factory = OTHER_ERROR_WITH_REASON,
      a =
        "@IdType class '${declaration.name}' must have exactly one property, found $propertyCount",
    )
  }
}
