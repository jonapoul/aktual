package aktual.compiler.id

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.plugin.createMemberFunction
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.types.constructType
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.util.OperatorNameConventions.COMPARE_TO
import org.jetbrains.kotlin.util.OperatorNameConventions.TO_STRING

internal class IdFirDeclarationGenerationExtension(session: FirSession) :
  FirDeclarationGenerationExtension(session) {
  override fun FirDeclarationPredicateRegistrar.registerPredicates() {
    register(IdPredicate)
  }

  // Property types aren't resolved yet at this stage, so whether the wrapped type is actually
  // Comparable can't be checked here; IdComparableChecker reports a compile error for that case
  // instead, once types are resolved.
  override fun getCallableNamesForClass(
    classSymbol: FirClassSymbol<*>,
    context: MemberGenerationContext,
  ): Set<Name> =
    if (session.predicateBasedProvider.matches(IdPredicate, classSymbol)) {
      setOf(TO_STRING, COMPARE_TO)
    } else {
      emptySet()
    }

  override fun generateFunctions(
    callableId: CallableId,
    context: MemberGenerationContext?,
  ): List<FirNamedFunctionSymbol> {
    val owner = context?.owner ?: return emptyList()
    val function =
      when (callableId.callableName) {
        TO_STRING -> generateToString(owner)
        COMPARE_TO -> generateCompareTo(owner)
        else -> return emptyList()
      }
    return listOf(function.symbol)
  }

  private fun generateToString(owner: FirClassSymbol<*>) =
    createMemberFunction(
      owner = owner,
      key = IdPluginKey,
      name = TO_STRING,
      returnType = session.builtinTypes.stringType.coneType,
      config = { status { isOverride = true } },
    )

  private fun generateCompareTo(owner: FirClassSymbol<*>) =
    createMemberFunction(
      owner = owner,
      key = IdPluginKey,
      name = COMPARE_TO,
      returnType = session.builtinTypes.intType.coneType,
      config = {
        status {
          isOverride = true
          isOperator = true
        }
        valueParameter(name = Name.identifier("other"), type = owner.constructType())
      },
    )
}
