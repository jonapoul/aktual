package aktual.codegen

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import kotlin.reflect.KClass

internal abstract class AnnotatedClassVisitor(val annotation: KClass<*>) : KSVisitorVoid() {
  abstract fun validate(annotated: KSAnnotated)
  abstract override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit)
}
