package aktual.detekt.rules

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

/** Flags assertk's `prop("name") { it.name }`. Use a property reference, `prop(Type::name)`. */
internal class StringPropName(config: Config) :
  Rule(
    config = config,
    description =
      "Don't pass a name string to prop() - use a property reference like prop(Type::name)",
  ) {

  override fun visitCallExpression(expression: KtCallExpression) {
    super.visitCallExpression(expression)
    if (expression.calleeExpression?.text != "prop") return
    val firstArg = expression.valueArguments.firstOrNull()?.getArgumentExpression()
    if (firstArg is KtStringTemplateExpression) {
      report(Finding(entity = Entity.from(expression), message = description))
    }
  }
}
