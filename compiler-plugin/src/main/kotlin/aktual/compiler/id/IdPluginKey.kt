package aktual.compiler.id

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.name.FqName

internal object IdPluginKey : GeneratedDeclarationKey()

internal val IdPredicate = DeclarationPredicate.create {
  annotated(FqName("aktual.budget.model.IdType"))
}
