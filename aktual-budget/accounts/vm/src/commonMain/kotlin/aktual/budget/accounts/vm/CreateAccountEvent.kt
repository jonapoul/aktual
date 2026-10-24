package aktual.budget.accounts.vm

import aktual.budget.model.AccountId

sealed interface CreateAccountEvent {
  data class Created(val id: AccountId) : CreateAccountEvent
}
