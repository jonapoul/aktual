package aktual.detekt

import aktual.detekt.rules.InvalidComposeLazyKey
import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

class AktualRuleSetProvider : RuleSetProvider {
  override val ruleSetId: RuleSetId = RuleSetId("aktual")

  override fun instance(): RuleSet =
    RuleSet(id = ruleSetId, rules = listOf(::InvalidComposeLazyKey))
}
