package aktual.codegen

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

internal class AdaptedApiVisitor(
  private val codeGenerator: CodeGenerator,
  private val logger: KSPLogger,
) : AnnotatedClassVisitor(AdaptedApi::class) {
  override fun validate(annotated: KSAnnotated) = with(annotated) {
    if (this !is KSClassDeclaration) {
      error("$this is not a class!")
    } else if (classKind != ClassKind.INTERFACE) {
      error("$this should be an interface, was actually a $classKind")
    }
  }

  override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
    logger.logging("visitClassDeclaration $classDeclaration")
    val receiverType = classDeclaration.toClassName()
    val functions = classDeclaration
      .getDeclaredFunctions()
      .mapNotNull { function -> function.toFunSpec(receiverType) }
      .toList()

    if (functions.isEmpty()) {
      logger.info("No functions to be generated for $receiverType")
      return
    }

    logger.info("Generating ${functions.size} adapter functions for $receiverType")
    FileSpec
      .builder(classDeclaration.packageName.asString(), classDeclaration.simpleName.asString())
      .addFunctions(functions)
      .build()
      .writeTo(codeGenerator, aggregating = true, classDeclaration.originatingFiles())
  }

  private fun KSFunctionDeclaration.toFunSpec(receiverType: ClassName): FunSpec? {
    val resolvedReturnType = returnType?.resolve() ?: error("No return type for $simpleName")
    val originalReturnType = resolvedReturnType.toTypeName()
    if (originalReturnType == RESPONSE_RESPONSEBODY) {
      logger.info("Not generating adapter extension for ${qualifiedName?.asString()} - returns Response<ResponseBody>")
      return null
    }

    val typeParameters = resolvedReturnType.arguments.mapNotNull { it.type?.resolve() }
    if (typeParameters.size != 1) {
      logger.warn("Found ${typeParameters.size} parameters in $resolvedReturnType, needed 1")
      return null
    }
    val successType = typeParameters.first()
    val sealedInterface = resolveSealedInterfaceSuperType(successType) ?: return null
    val failureType = resolveFailureType(sealedInterface, successType).toClassName()

    val params = parseParameters()
    val codeBlock = CodeBlock.of(
      """return ${simpleName.asString()}(${params.joinToString { it.name }}).%M(%T.serializer())""",
      ADAPTED,
      failureType,
    )

    return FunSpec
      .builder(simpleName.asString() + "Adapted")
      .addModifiers(KModifier.SUSPEND)
      .receiver(receiverType)
      .returns(ACTUAL_RESPONSE.parameterizedBy(sealedInterface.toClassName()))
      .addParameters(params)
      .addCode(codeBlock)
      .build()
  }

  private fun KSFunctionDeclaration.parseParameters(): List<ParameterSpec> = parameters
    .onEach { if (it.hasDefault) error("Don't support generating default values for ${qualifiedName?.asString()}") }
    .map { it.toParameterSpec() }

  private fun KSValueParameter.toParameterSpec(): ParameterSpec {
    val resolvedType = type.resolve()
    val type = resolvedType.declaration.qualifiedName?.asString() ?: error("No type for $resolvedType")
    val className = if (resolvedType.isMarkedNullable) {
      ClassName.bestGuess(type).copy(nullable = true)
    } else {
      ClassName.bestGuess(type)
    }

    val paramName = name?.asString() ?: error("No parameter name for $this")
    return ParameterSpec
      .builder(paramName, className)
      .build()
  }

  private fun resolveSealedInterfaceSuperType(successType: KSType): KSClassDeclaration? {
    val successTypeDeclaration = successType.declaration as? KSClassDeclaration
      ?: error("${successType.declaration} is not a class")

    return successTypeDeclaration
      .superTypes
      .mapNotNull { it.resolve().declaration as? KSClassDeclaration }
      .firstOrNull { it.modifiers.contains(Modifier.SEALED) }
  }

  private fun resolveFailureType(sealedInterface: KSClassDeclaration, successType: KSType): KSType {
    val otherSubtypes = sealedInterface
      .getSealedSubclasses()
      .map { it.asType(emptyList()) }
      .filterNot { it == successType }
      .toList()
    return when (otherSubtypes.size) {
      0 -> error("No other subtypes found for ${sealedInterface.toClassName()}")
      1 -> otherSubtypes.first()
      else -> error("Too many subtypes found for ${sealedInterface.toClassName()}: $otherSubtypes")
    }
  }
}
