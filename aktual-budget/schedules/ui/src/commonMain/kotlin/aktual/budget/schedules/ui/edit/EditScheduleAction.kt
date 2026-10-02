package aktual.budget.schedules.ui.edit

import aktual.budget.model.AccountId
import aktual.budget.model.PayeeId
import aktual.budget.schedules.vm.edit.ScheduleAmount
import aktual.budget.schedules.vm.edit.ScheduleDate
import androidx.compose.runtime.Immutable

@Immutable internal sealed interface EditScheduleAction

internal data object NavigateBack : EditScheduleAction

internal data object StartEditing : EditScheduleAction

internal data object StopEditing : EditScheduleAction

internal data object SaveSchedule : EditScheduleAction

internal data object DeleteSchedule : EditScheduleAction

internal data object DismissError : EditScheduleAction

@JvmInline internal value class SetName(val name: String) : EditScheduleAction

@JvmInline internal value class SetPayee(val id: PayeeId) : EditScheduleAction

@JvmInline internal value class SetAccount(val id: AccountId) : EditScheduleAction

@JvmInline internal value class SetAmount(val amount: ScheduleAmount) : EditScheduleAction

@JvmInline internal value class SetDate(val date: ScheduleDate) : EditScheduleAction

@JvmInline internal value class SetPostsTransaction(val posts: Boolean) : EditScheduleAction

@Immutable
internal fun interface EditScheduleActionHandler {
  operator fun invoke(action: EditScheduleAction)
}
