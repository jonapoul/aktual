package aktual.budget.home.ui

import aktual.budget.home.vm.HomeCardType
import aktual.budget.model.AccountId
import aktual.budget.model.ScheduleId
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

internal sealed interface HomeAction

@JvmInline internal value class OpenAccount(val id: AccountId) : HomeAction

internal data object OpenBankSync : HomeAction

@JvmInline internal value class OpenBankSyncSettings(val id: AccountId) : HomeAction

@JvmInline internal value class LinkBankAccount(val id: AccountId) : HomeAction

internal data object ReviewUncategorised : HomeAction

@JvmInline internal value class OpenSchedule(val id: ScheduleId) : HomeAction

internal data object OpenSchedules : HomeAction

internal data object Retry : HomeAction

internal data class SwapCards(val card: HomeCardType, val other: HomeCardType) : HomeAction

@JvmInline internal value class ReorderCards(val cards: ImmutableList<HomeCardType>) : HomeAction

@Immutable
internal fun interface HomeActionHandler {
  operator fun invoke(action: HomeAction)
}
