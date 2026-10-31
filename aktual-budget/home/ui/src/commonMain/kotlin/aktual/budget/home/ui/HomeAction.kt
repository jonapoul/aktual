package aktual.budget.home.ui

import aktual.budget.home.vm.HomeCardType
import aktual.budget.model.AccountId
import aktual.budget.model.ScheduleId
import aktual.budget.model.WidgetId
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

internal sealed interface HomeAction

@JvmInline internal value class OpenAccount(val id: AccountId) : HomeAction

@JvmInline internal value class NewTransaction(val account: AccountId) : HomeAction

internal data object OpenBankSync : HomeAction

internal data object AddAccount : HomeAction

@JvmInline internal value class OpenBankSyncSettings(val id: AccountId) : HomeAction

@JvmInline internal value class LinkBankAccount(val id: AccountId) : HomeAction

internal data object ReviewUncategorised : HomeAction

@JvmInline internal value class OpenSchedule(val id: ScheduleId) : HomeAction

internal data object OpenSchedules : HomeAction

internal data object Retry : HomeAction

@JvmInline internal value class OpenReport(val id: WidgetId) : HomeAction

@JvmInline internal value class PinReport(val id: WidgetId) : HomeAction

@JvmInline internal value class UnpinReport(val id: WidgetId) : HomeAction

internal data class ReplaceReport(val old: WidgetId, val new: WidgetId) : HomeAction

internal data class SwapCards(val card: HomeCardType, val other: HomeCardType) : HomeAction

@JvmInline internal value class ReorderCards(val cards: ImmutableList<HomeCardType>) : HomeAction

@Immutable
internal fun interface HomeActionHandler {
  operator fun invoke(action: HomeAction)
}
