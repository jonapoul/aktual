package aktual.detekt.rules

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression

/**
 * Flags `Dispatchers.setMain`. VMs take a `ViewModelContext` instead, so tests pass the test
 * dispatcher in directly, e.g. with the `TestScope.viewModelContext` helper.
 */
internal class SetMainDispatcher(config: Config) :
  Rule(
    config = config,
    description =
      "Don't use Dispatchers.setMain - pass a ViewModelContext into the ViewModel instead",
  ) {

  override fun visitCallExpression(expression: KtCallExpression) {
    super.visitCallExpression(expression)
    if (expression.calleeExpression?.text == "setMain") {
      report(Finding(entity = Entity.from(expression), message = description))
    }
  }
}
