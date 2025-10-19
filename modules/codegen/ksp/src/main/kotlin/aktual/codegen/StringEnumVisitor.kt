/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package aktual.codegen

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo

internal class StringEnumVisitor(
  private val codeGenerator: CodeGenerator,
  private val logger: KSPLogger,
) : AnnotatedClassVisitor(GenerateParser::class) {
  override fun validate(annotated: KSAnnotated) = with(annotated) {
    if (this !is KSClassDeclaration) {
      error("$this is not a class!")
    } else if (classKind != ClassKind.ENUM_CLASS) {
      error("$this should be an enum, was actually a $classKind")
    } else if (!implements(STRING_VALUE)) {
      error("$this should inherit from $STRING_VALUE")
    }
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
          ?: error("No %L matching '$$paramName'")
      """.trimIndent(),
      className,
      className.simpleName,
    )

    val function = FunSpec
      .builder("parse")
      .addParameter(parameter)
      .receiver(classDeclaration.companionObject.toClassName())
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
}
