package aktual.compiler.id

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.plugin.createConeType
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.isSubtypeOf
import org.jetbrains.kotlin.name.StandardClassIds

// The wrapped value is always the class's single declared property, e.g. `value` in `AccountId(val
// value: String)`
@OptIn(DirectDeclarationsAccess::class)
internal fun FirClassSymbol<*>.resolvedValuePropertyType(): ConeKotlinType? =
  declarationSymbols.filterIsInstance<FirPropertySymbol>().singleOrNull()?.resolvedReturnType

// True if this type has a usable `compareTo(self)`, i.e. it implements Comparable<TypeOfSelf>
internal fun ConeKotlinType.isComparableToSelf(session: FirSession): Boolean {
  val comparableOfSelf = StandardClassIds.Comparable.createConeType(session, arrayOf(this))
  return isSubtypeOf(comparableOfSelf, session)
}
