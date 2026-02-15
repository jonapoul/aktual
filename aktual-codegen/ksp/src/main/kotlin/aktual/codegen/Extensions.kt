package aktual.codegen

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toTypeName

internal fun KSDeclaration.originatingFiles() = containingFile?.let { listOf(it) }.orEmpty()

internal val KSClassDeclaration.companionObject
  get() =
    declarations.filterIsInstance<KSClassDeclaration>().firstOrNull { it.isCompanionObject }
      ?: error("No companion object on $this")

internal fun KSClassDeclaration.implements(type: ClassName) =
  superTypes.any { it.toTypeName() == type }

internal fun KSAnnotation.getValue() =
  arguments.firstOrNull { it.name.requireString == "value" }?.value?.toString()
    ?: error("No name found for ${shortName.requireString}'s arguments")

internal val KSAnnotation.qualifiedName: String
  get() = annotationType.resolve().declaration.qualifiedName.requireString

internal val KSName?.requireString: String
  get() = this?.asString() ?: error("Required string, got null")
