package aktual.budget.home.ui

import aktual.budget.model.AccountId
import aktual.budget.model.ScheduleId
import androidx.compose.runtime.Immutable

internal sealed interface HomeAction

@JvmInline internal value class OpenAccount(val id: AccountId) : HomeAction

internal data object OpenBankSync : HomeAction

@JvmInline internal value class OpenBankSyncSettings(val id: AccountId) : HomeAction

@JvmInline internal value class LinkBankAccount(val id: AccountId) : HomeAction

internal data object ReviewUncategorised : HomeAction

@JvmInline internal value class OpenSchedule(val id: ScheduleId) : HomeAction

internal data object OpenSchedules : HomeAction

internal data object Retry : HomeAction

@Immutable
internal fun interface HomeActionHandler {
  operator fun invoke(action: HomeAction)
}
