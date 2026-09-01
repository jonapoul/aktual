package aktual.compiler.id

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirClassLikeDeclaration
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.FirSupertypeGenerationExtension
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.plugin.createConeType
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.FirResolvedTypeRef
import org.jetbrains.kotlin.fir.types.constructType
import org.jetbrains.kotlin.name.StandardClassIds.Comparable

internal class IdFirSupertypeGenerationExtension(session: FirSession) :
  FirSupertypeGenerationExtension(session) {
  override fun FirDeclarationPredicateRegistrar.registerPredicates() {
    register(IdPredicate)
  }

  override fun needTransformSupertypes(declaration: FirClassLikeDeclaration): Boolean =
    session.predicateBasedProvider.matches(IdPredicate, declaration)

  // Property types aren't resolved yet at this stage, so whether the wrapped type is actually
  // Comparable can't be
  // checked here; IdComparableChecker reports a compile error for that case instead, once types are
  // resolved.
  override fun computeAdditionalSupertypes(
    classLikeDeclaration: FirClassLikeDeclaration,
    resolvedSupertypes: List<FirResolvedTypeRef>,
    typeResolver: TypeResolveService,
  ): List<ConeKotlinType> {
    val selfType = classLikeDeclaration.symbol.constructType()
    val comparableType = Comparable.createConeType(session, arrayOf(selfType))
    return listOf(comparableType)
  }
}
