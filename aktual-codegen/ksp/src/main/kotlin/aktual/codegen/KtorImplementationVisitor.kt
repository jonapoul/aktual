package aktual.codegen

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import kotlin.reflect.KClass

internal class KtorImplementationVisitor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : AnnotatedClassVisitor(KtorApi::class) {
  override fun validate(annotated: KSAnnotated) =
      with(annotated) {
        if (this !is KSClassDeclaration) {
          error("$this is not a class!")
        } else if (classKind != ClassKind.INTERFACE) {
          error("$this should be an interface, was actually a $classKind")
        }
      }

  override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
    logger.info("visitClassDeclaration $classDeclaration")

    val functions =
        classDeclaration
            .getDeclaredFunctions()
            .map { f -> buildFunction(f, f.getMethod()) }
            .toList()

    logger.info("Found annotated functions ${functions.joinToString { it.name }}")

    val apiClassName = classDeclaration.toClassName()
    val type = buildImplType(apiClassName, functions)
    val function = buildFactoryFunction(apiClassName, type)

    logger.info("Writing ${function.name} and ${type.name} for $apiClassName")

    FileSpec.builder(
            classDeclaration.packageName.requireString,
            classDeclaration.simpleName.requireString + "Impl",
        )
        .addFunction(function)
        .addAnnotation(buildSuppressionAnnotation())
        .addType(type)
        .build()
        .writeTo(codeGenerator, aggregating = true, classDeclaration.originatingFiles())
  }

  private fun buildSuppressionAnnotation() =
      AnnotationSpec.builder(ClassName("kotlin", "Suppress"))
          .apply { FILE_SUPPRESSIONS.forEach { addMember("\"$it\"") } }
          .build()

  private fun buildImplType(apiClassName: ClassName, functions: List<FunSpec>) =
      TypeSpec.classBuilder(ClassName(apiClassName.packageName, apiClassName.simpleName + "Client"))
          .addModifiers(PRIVATE)
          .addSuperinterface(apiClassName)
          .addSuperinterface(TYPE_AUTOCLOSEABLE, delegate = CodeBlock.of(PROPERTY_CLIENT))
          .primaryConstructor(buildConstructor())
          .addProperty(
              PropertySpec.builder(PROPERTY_SERVER_URL, TYPE_SERVER_URL, PRIVATE)
                  .initializer(PROPERTY_SERVER_URL)
                  .build(),
          )
          .addProperty(
              PropertySpec.builder(PROPERTY_CLIENT, TYPE_HTTP_CLIENT, PRIVATE)
                  .initializer(PROPERTY_CLIENT)
                  .build(),
          )
          .addFunctions(functions)
          .addProperty(buildUrlProtocolProperty())
          .build()

  private fun buildUrlProtocolProperty() =
      PropertySpec.builder(PROPERTY_URL_PROTOCOL, TYPE_URL_PROTOCOL, PRIVATE)
          .initializer(
              CodeBlock.builder()
                  .beginControlFlow("when ($PROPERTY_SERVER_URL.protocol) {")
                  .addStatement("%T.Http -> %T.HTTP", TYPE_PROTOCOL, TYPE_URL_PROTOCOL)
                  .addStatement("%T.Https -> %T.HTTPS", TYPE_PROTOCOL, TYPE_URL_PROTOCOL)
                  .endControlFlow()
                  .build(),
          )
          .build()

  /**
   * TODO: Shouldn't need to add [KModifier.ACTUAL] here, but KSP doesn't support generating code
   *   into the commonMain source set. This means we need declarations in each separate source set,
   *   then using expect/actual to tie them together and make them callable from the common source
   *   set. See https://github.com/google/ksp/issues/567
   */
  private fun buildFactoryFunction(receiverType: ClassName, implType: TypeSpec) =
      FunSpec.builder(receiverType.simpleName)
          .returns(receiverType)
          .addModifiers(KModifier.ACTUAL)
          .addParameter(PROPERTY_SERVER_URL, TYPE_SERVER_URL)
          .addParameter(PROPERTY_CLIENT, TYPE_HTTP_CLIENT)
          .addCode("return ${implType.name}($PROPERTY_SERVER_URL, $PROPERTY_CLIENT)")
          .build()

  private fun buildConstructor() =
      FunSpec.constructorBuilder()
          .addParameter(PROPERTY_SERVER_URL, TYPE_SERVER_URL)
          .addParameter(PROPERTY_CLIENT, TYPE_HTTP_CLIENT)
          .build()

  private fun buildFunction(f: KSFunctionDeclaration, annotation: Pair<Method, String>): FunSpec {
    val returnType = f.returnType?.resolve()?.toTypeName() ?: error("No return type for $f?")
    val parameterAnnotations = f.parameterAnnotations()
    logger.info("parameterAnnotations for ${f.simpleName.requireString} = $parameterAnnotations")

    val builder =
        FunSpec.builder(f.simpleName.requireString)
            .addModifiers(KModifier.OVERRIDE)
            .returns(returnType)

    if (Modifier.SUSPEND in f.modifiers) {
      builder.addModifiers(KModifier.SUSPEND)
    }

    f.parameters.forEach { p ->
      val type = p.type.resolve()
      val declaration = type.declaration
      val className = ClassName.bestGuess(declaration.qualifiedName.requireString)
      val parameter =
          ParameterSpec.builder(
                  p.name.requireString,
                  className.copy(nullable = type.isMarkedNullable),
              )
              .build()
      builder.addParameter(parameter)
    }

    val (method, path) = annotation
    builder.addCode(buildCode(returnType, parameterAnnotations, method, path))

    return builder.build()
  }

  private fun buildCode(
      returnType: TypeName,
      parameters: ParameterAnnotations,
      method: Method,
      path: String,
  ): CodeBlock {
    val builder =
        CodeBlock.builder().beginControlFlow("return $PROPERTY_CLIENT.%M {", method.memberName)

    // URL
    var replacedPath = path
    for (item in parameters.paths) replacedPath =
        replacedPath.replace("{${item.label}}", "$${item.variableName}")
    with(builder) {
      beginControlFlow("url {")
      add("protocol = $PROPERTY_URL_PROTOCOL")
      addStatement("")
      addStatement("host = $PROPERTY_SERVER_URL.baseUrl")
      addStatement("%M(\"$replacedPath\")", MEMBER_PATH)
      endControlFlow()
    }

    // Headers
    with(builder) {
      parameters.headers.forEach { (key, parameterName) ->
        addStatement("%M(\"$key\", $parameterName)", MEMBER_HEADER)
      }
    }

    // Body
    with(builder) {
      if (parameters.body != null) {
        // TODO: make this configurable
        addStatement("%M(%T.Application.Json)", MEMBER_CONTENT_TYPE, TYPE_CONTENT_TYPE)
        addStatement("%M(${parameters.body})", MEMBER_SET_BODY)
      }
    }

    // Parameters
    with(builder) {
      parameters.queries.forEach { (label, parameter) ->
        addStatement("%M(\"$label\", $parameter)", MEMBER_PARAMETER)
      }
    }

    builder.endControlFlow()

    if (returnType != TYPE_HTTP_RESPONSE) {
      builder.add(".%M<%T>()", MEMBER_BODY, returnType)
    }

    return builder.build()
  }

  private data class ParameterAnnotations(
      val body: String?,
      val headers: List<HeaderItem>,
      val paths: List<PathItem>,
      val queries: List<QueryItem>,
  )

  private data class HeaderItem(
      val headerName: String,
      val parameterName: String,
  )

  private data class PathItem(
      val label: String,
      val variableName: String,
  )

  private data class QueryItem(
      val label: String,
      val variableName: String,
  )

  private fun KSFunctionDeclaration.parameterAnnotations(): ParameterAnnotations {
    val bodies = arrayListOf<String>()
    val headers = arrayListOf<HeaderItem>()
    val paths = arrayListOf<PathItem>()
    val queries = arrayListOf<QueryItem>()

    for (parameter in parameters) {
      val list = parameter.annotations.toList()
      if (list.size > 1)
          error("Only support 1 parameter annotation on ${parameter.name.requireString}, got $list")
      val annotation = list.firstOrNull() ?: continue

      val qualifiedName =
          annotation.annotationType.resolve().declaration.qualifiedName.requireString
      val name = parameter.name.requireString
      when (qualifiedName) {
        Header::class.qualifiedName -> {
          headers +=
              HeaderItem(
                  headerName = annotation.getValue(),
                  parameterName = name,
              )
        }

        Path::class.qualifiedName -> {
          paths +=
              PathItem(
                  label = annotation.getValue(),
                  variableName = name,
              )
        }

        Query::class.qualifiedName -> {
          queries +=
              QueryItem(
                  label = annotation.getValue(),
                  variableName = name,
              )
        }

        Body::class.qualifiedName -> {
          bodies += parameter.toString()
        }
      }
    }

    if (bodies.size > 1) error("Only support one @Body parameter on ${simpleName.requireString}")
    return ParameterAnnotations(bodies.firstOrNull(), headers, paths, queries)
  }

  private fun KSAnnotation.getValue() =
      arguments.firstOrNull { it.name.requireString == "value" }?.value?.toString()
          ?: error("No name found for ${shortName.requireString}'s arguments")

  private val KSAnnotation.qualifiedName: String?
    get() = annotationType.resolve().declaration.qualifiedName.requireString

  private val KSName?.requireString: String
    get() = this?.asString() ?: error("Required string, got null")

  private fun KSFunctionDeclaration.getMethod(): Pair<Method, String> {
    for (annotation in annotations) {
      Method.entries.forEach { m ->
        if (m.annotation.qualifiedName == annotation.qualifiedName) {
          val path =
              annotation.arguments.first { it.name.requireString == "path" }.value?.toString()
                  ?: error(
                      "No value found for path parameter in ${annotation.shortName.requireString}"
                  )
          return m to path
        }
      }
    }
    error("No method annotation found on ${simpleName.requireString}")
  }

  private enum class Method(val annotation: KClass<*>, val memberName: MemberName) {
    Delete(DELETE::class, MEMBER_DELETE),
    Get(GET::class, MEMBER_GET),
    Patch(PATCH::class, MEMBER_PATCH),
    Post(POST::class, MEMBER_POST),
    Put(PUT::class, MEMBER_PUT),
  }

  private companion object {
    val TYPE_AUTOCLOSEABLE = ClassName("kotlin", "AutoCloseable")
    val TYPE_CONTENT_TYPE = ClassName("io.ktor.http", "ContentType")
    val TYPE_HTTP_CLIENT = ClassName("io.ktor.client", "HttpClient")
    val TYPE_HTTP_RESPONSE = ClassName("io.ktor.client.statement", "HttpResponse")
    val TYPE_PROTOCOL = ClassName("aktual.core.model", "Protocol")
    val TYPE_SERVER_URL = ClassName("aktual.core.model", "ServerUrl")
    val TYPE_URL_PROTOCOL = ClassName("io.ktor.http", "URLProtocol")

    val MEMBER_BODY = MemberName("io.ktor.client.call", "body")
    val MEMBER_CONTENT_TYPE = MemberName("io.ktor.http", "contentType")
    val MEMBER_DELETE = MemberName("io.ktor.client.request", "delete")
    val MEMBER_GET = MemberName("io.ktor.client.request", "get")
    val MEMBER_HEADER = MemberName("io.ktor.client.request", "header")
    val MEMBER_PARAMETER = MemberName("io.ktor.client.request", "parameter")
    val MEMBER_PATCH = MemberName("io.ktor.client.request", "patch")
    val MEMBER_PATH = MemberName("io.ktor.http", "path")
    val MEMBER_POST = MemberName("io.ktor.client.request", "post")
    val MEMBER_PUT = MemberName("io.ktor.client.request", "put")
    val MEMBER_SET_BODY = MemberName("io.ktor.client.request", "setBody")

    const val PROPERTY_URL_PROTOCOL = "urlProtocol"
    const val PROPERTY_CLIENT = "client"
    const val PROPERTY_SERVER_URL = "serverUrl"

    val FILE_SUPPRESSIONS =
        listOf(
            "ALL",
            "warnings",
            "RemoveRedundantBackticks",
        )
  }
}
