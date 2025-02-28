package actual.codegen

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

@Suppress("UnsafeCallOnNullableType")
internal class ActualSymbolProcessor(
  private val codeGenerator: CodeGenerator,
  private val logger: KSPLogger,
) : KSVisitorVoid(), SymbolProcessor {
  override fun process(resolver: Resolver): List<KSAnnotated> {
    resolver
      .getSymbolsWithAnnotation(GenerateParser::class.qualifiedName!!)
      .onEach { it.validate() }
      .filterIsInstance<KSClassDeclaration>()
      .forEach { it.accept(visitor = this, Unit) }

    return emptyList()
  }

  override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
    logger.logging("visitClassDeclaration $classDeclaration")

    val className = classDeclaration.toClassName()
    val paramName = "string"
    val stringPropertyName = StringValue::value.name
    val parameter = ParameterSpec
      .builder(paramName, String::class.asClassName())
      .build()

    val codeBlock = CodeBlock.of(
      """
        return %T
          .entries
          .firstOrNull { it.$stringPropertyName == $paramName }
          ?: error("No %L matching '${"\$"}$paramName'")
      """.trimIndent(),
      className,
      className.simpleName,
    )

    val function = FunSpec
      .builder("parse")
      .addParameter(parameter)
      .receiver(classDeclaration.companionObject().toClassName())
      .returns(className)
      .addCode(codeBlock)
      .build()

    FileSpec
      .builder(classDeclaration.packageName.asString(), classDeclaration.simpleName.asString())
      .addFunction(function)
      .build()
      .writeTo(codeGenerator, aggregating = true, classDeclaration.originatingFiles())

    logger.info("Generated ${function.name}")
  }

  private fun KSAnnotated.validate() {
    if (this !is KSClassDeclaration) {
      error("$this is not a class!")
    } else if (classKind != ClassKind.ENUM_CLASS) {
      error("$this should be an enum, was actually a $classKind")
    } else if (!implements(STRING_VALUE)) {
      error("$this should inherit from $STRING_VALUE")
    }
  }

  private fun KSDeclaration.originatingFiles() = containingFile?.let { listOf(it) }.orEmpty()

  private fun KSClassDeclaration.implements(type: ClassName) = superTypes.any { it.toTypeName() == type }

  private fun KSClassDeclaration.companionObject() = declarations
    .filterIsInstance<KSClassDeclaration>()
    .firstOrNull { it.isCompanionObject }
    ?: error("No companion object on $this")

  private companion object {
    val STRING_VALUE = StringValue::class.asClassName()
  }
}
