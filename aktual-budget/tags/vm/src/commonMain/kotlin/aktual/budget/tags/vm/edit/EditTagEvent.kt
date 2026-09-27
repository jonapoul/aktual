package aktual.budget.tags.vm.edit

sealed interface EditTagEvent {
  data object FinishedSaving : EditTagEvent
}
