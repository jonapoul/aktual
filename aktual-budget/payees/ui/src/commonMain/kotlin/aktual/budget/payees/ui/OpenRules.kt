package aktual.budget.payees.ui

import aktual.budget.payees.vm.list.PayeeItem
import aktual.core.nav.EditRuleNavigator
import aktual.core.nav.SearchRulesNavigator

// Like upstream's rule chip: create a rule for the payee when it has none, otherwise list them
internal fun openRules(
  payee: PayeeItem,
  editRule: EditRuleNavigator,
  searchRules: SearchRulesNavigator,
) {
  if (payee.numRules == 0) editRule(payee = payee.id) else searchRules(payee = payee.id)
}
