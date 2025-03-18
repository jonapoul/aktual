package actual.codegen

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

@Suppress("UnsafeCallOnNullableType")
internal class ActualSymbolProcessor(
  private val codeGenerator: CodeGenerator,
  private val logger: KSPLogger,
) : SymbolProcessor {
  override fun process(resolver: Resolver): List<KSAnnotated> {
    listOf(
      StringEnumVisitor(codeGenerator, logger),
      AdaptedApiVisitor(codeGenerator, logger),
    ).forEach { visitor ->
      resolver
        .getSymbolsWithAnnotation(visitor.annotation.qualifiedName!!)
        .onEach(visitor::validate)
        .filterIsInstance<KSClassDeclaration>()
        .forEach { it.accept(visitor, Unit) }
    }
    return emptyList()
  }
}
