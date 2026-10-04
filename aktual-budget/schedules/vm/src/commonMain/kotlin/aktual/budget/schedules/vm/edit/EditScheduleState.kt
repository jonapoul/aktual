package aktual.budget.schedules.vm.edit

import aktual.budget.model.AccountId
import aktual.budget.model.PayeeId
import aktual.budget.schedules.domain.ScheduleStatus
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate

@Immutable
sealed interface EditScheduleState {
  data object Loading : EditScheduleState

  sealed interface Failure : EditScheduleState {
    data object NotFound : Failure

    @JvmInline value class Other(val reason: String) : Failure
  }

  data class Loaded(
    val form: ScheduleForm,
    val isNew: Boolean,
    val isEditing: Boolean,
    val hasChanges: Boolean,
    val isWorking: Boolean,
    val status: ScheduleStatus?,
    val payeeName: String?,
    val accountName: String?,
    val payees: ImmutableList<NamedEntity<PayeeId>>,
    val accounts: ImmutableList<NamedEntity<AccountId>>,
    val upcomingDates: ImmutableList<LocalDate>,
  ) : EditScheduleState {
    // Schedules without a payee or account are left out of the list, so both are required here
    val canSave: Boolean
      get() = !isWorking && form.payee != null && form.account != null && (isNew || hasChanges)
  }
}

@Immutable
sealed interface EditScheduleError {
  data class DuplicateName(val name: String) : EditScheduleError

  data class Saving(val reason: String) : EditScheduleError

  data class Deleting(val reason: String) : EditScheduleError
}

sealed interface EditScheduleEvent {
  data object Created : EditScheduleEvent

  data object Deleted : EditScheduleEvent
}
