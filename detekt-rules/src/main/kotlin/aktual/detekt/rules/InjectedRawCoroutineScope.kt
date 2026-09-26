package aktual.detekt.rules

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter

/**
 * Flags a plain `CoroutineScope` injected via a Metro constructor or `@Provides` function. Inject a
 * scoped type like `AppCoroutineScope` or `BudgetCoroutineScope` instead, so the work is cancelled
 * along with its DI scope.
 */
internal class InjectedRawCoroutineScope(config: Config) :
  Rule(
    config = config,
    description =
      "Inject a scoped type like AppCoroutineScope or BudgetCoroutineScope instead of a plain CoroutineScope",
  ) {

  override fun visitClass(klass: KtClass) {
    super.visitClass(klass)

    val injectedSecondaries = klass.secondaryConstructors.filter { it.hasAnnotation(INJECT) }
    val params =
      if (injectedSecondaries.isNotEmpty()) {
        injectedSecondaries.flatMap { it.valueParameters }
      } else if (klass.isInjected()) {
        klass.primaryConstructorParameters
      } else {
        emptyList()
      }

    params.forEach(::check)
  }

  override fun visitNamedFunction(function: KtNamedFunction) {
    super.visitNamedFunction(function)
    if (function.hasAnnotation(PROVIDES)) function.valueParameters.forEach(::check)
  }

  private fun KtClass.isInjected(): Boolean =
    primaryConstructor?.hasAnnotation(INJECT) == true || CLASS_ANNOTATIONS.any { hasAnnotation(it) }

  private fun check(param: KtParameter) {
    val type = param.typeReference?.text?.removeSuffix("?") ?: return
    if (type in RAW_TYPES) report(Finding(entity = Entity.from(param), message = description))
  }

  private fun KtAnnotated.hasAnnotation(name: String): Boolean = annotationEntries.any {
    it.shortName?.asString() == name
  }

  private companion object {
    const val INJECT = "Inject"
    const val PROVIDES = "Provides"

    val CLASS_ANNOTATIONS =
      setOf(
        INJECT,
        "AssistedInject",
        "ContributesBinding",
        "ContributesIntoMap",
        "ContributesIntoSet",
        "SingleIn",
      )

    val RAW_TYPES = setOf("CoroutineScope", "kotlinx.coroutines.CoroutineScope")
  }
}
