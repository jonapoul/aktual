package aktual.detekt.rules

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression

/**
 * Intended for compose LazyColumn items, might match others too? We'll see
 *
 * Will update if so
 */
internal class NonPrimitiveKey(config: Config) :
  Rule(
    config = config,
    description =
      "Lazy list key lambdas should return a primitive type (Int, Long, String, etc.) for stable keys",
  ),
  RequiresAnalysisApi {

  override fun visitCallExpression(expression: KtCallExpression) {
    super.visitCallExpression(expression)

    val keyArg =
      expression.valueArguments.firstOrNull { it.getArgumentName()?.asName?.asString() == "key" }
        ?: return

    val lambda = keyArg.getArgumentExpression() as? KtLambdaExpression ?: return
    val lastStatement = lambda.bodyExpression?.statements?.lastOrNull() ?: return

    analyze(expression) {
      val fqName =
        lastStatement.expressionType?.expandedSymbol?.classId?.asSingleFqName()?.asString()
          ?: return@analyze

      if (fqName !in PRIMITIVE_TYPES) {
        report(Finding(entity = Entity.from(keyArg), message = description))
      }
    }
  }

  private companion object {
    val PRIMITIVE_TYPES =
      setOf(
        "kotlin.Boolean",
        "kotlin.Byte",
        "kotlin.Char",
        "kotlin.Double",
        "kotlin.Float",
        "kotlin.Int",
        "kotlin.Long",
        "kotlin.Short",
        "kotlin.String",
      )
  }
}
