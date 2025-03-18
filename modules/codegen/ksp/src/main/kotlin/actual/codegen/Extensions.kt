package actual.codegen

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toTypeName

internal fun KSDeclaration.originatingFiles() = containingFile?.let { listOf(it) }.orEmpty()

internal val KSClassDeclaration.companionObject
  get() = declarations
    .filterIsInstance<KSClassDeclaration>()
    .firstOrNull { it.isCompanionObject }
    ?: error("No companion object on $this")

internal fun KSClassDeclaration.implements(type: ClassName) = superTypes.any { it.toTypeName() == type }
