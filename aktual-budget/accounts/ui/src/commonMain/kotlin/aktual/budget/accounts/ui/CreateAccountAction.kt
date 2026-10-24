package aktual.budget.accounts.ui

import androidx.compose.runtime.Immutable

@Immutable internal sealed interface CreateAccountAction

internal data object NavigateBack : CreateAccountAction

internal data object Create : CreateAccountAction

internal data object DismissSaveError : CreateAccountAction

@JvmInline internal value class SetName(val name: String) : CreateAccountAction

@JvmInline internal value class SetBalance(val balance: String) : CreateAccountAction

@JvmInline internal value class SetOffBudget(val offBudget: Boolean) : CreateAccountAction

@JvmInline internal value class SetNegative(val negative: Boolean) : CreateAccountAction

@Immutable
internal fun interface CreateAccountActionHandler {
  operator fun invoke(action: CreateAccountAction)
}
