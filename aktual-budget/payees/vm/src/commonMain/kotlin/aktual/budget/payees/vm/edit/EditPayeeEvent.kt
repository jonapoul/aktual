package aktual.budget.payees.vm.edit

sealed interface EditPayeeEvent {
  data object FinishedSaving : EditPayeeEvent
}
