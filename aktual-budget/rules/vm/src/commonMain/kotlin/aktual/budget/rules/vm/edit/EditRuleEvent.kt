package aktual.budget.rules.vm.edit

sealed interface EditRuleEvent {
  data object DeletedRule : EditRuleEvent

  @JvmInline value class AppliedActions(val count: Int) : EditRuleEvent

  data object ApplyFailed : EditRuleEvent
}
