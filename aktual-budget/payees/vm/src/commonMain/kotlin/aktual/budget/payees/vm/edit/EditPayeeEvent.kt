package aktual.budget.payees.vm.edit

import aktual.budget.model.PayeeLocationId

sealed interface EditPayeeEvent {
  data object FinishedSaving : EditPayeeEvent

  @JvmInline value class LocationDeleted(val id: PayeeLocationId) : EditPayeeEvent

  data object LocationDeleteFailed : EditPayeeEvent

  data object LocationRestoreFailed : EditPayeeEvent
}
