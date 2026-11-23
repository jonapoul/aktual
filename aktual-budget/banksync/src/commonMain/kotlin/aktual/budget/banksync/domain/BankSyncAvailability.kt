package aktual.budget.banksync.domain

import aktual.budget.model.BudgetId
import aktual.core.model.BudgetServer

// Bank sync goes through the server, except in the demo budget, which fakes the bank's responses
fun BudgetServer.canBankSync(budget: BudgetId): Boolean = this is Remote || budget == Demo
