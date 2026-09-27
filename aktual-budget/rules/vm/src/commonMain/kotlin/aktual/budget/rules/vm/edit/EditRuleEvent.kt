package aktual.budget.rules.vm.edit

sealed interface EditRuleEvent {
  data object DeletedRule : EditRuleEvent
}
