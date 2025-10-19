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
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

internal class AktualSymbolProcessor(
  private val codeGenerator: CodeGenerator,
  private val logger: KSPLogger,
) : SymbolProcessor {
  override fun process(resolver: Resolver): List<KSAnnotated> {
    logger.info("AktualSymbolProcessor process")
    listOf(
      StringEnumVisitor(codeGenerator, logger),
      AdaptedApiVisitor(codeGenerator, logger),
      KtorImplementationVisitor(codeGenerator, logger),
    ).forEach { visitor ->
      resolver
        .getSymbolsWithAnnotation(requireNotNull(visitor.annotation.qualifiedName))
        .onEach(visitor::validate)
        .filterIsInstance<KSClassDeclaration>()
        .forEach { it.accept(visitor, Unit) }
    }
    return emptyList()
  }
}
