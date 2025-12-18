package aktual.test

import aktual.budget.model.AccountSpec
import aktual.budget.model.BudgetId
import aktual.budget.model.TransactionsSpec
import aktual.core.model.LoginToken

internal val LOGIN_TOKEN = LoginToken("abc-123")
internal val BUDGET_ID = BudgetId("abc-123")
internal val TRANSACTIONS_SPEC = TransactionsSpec(AccountSpec.AllAccounts)
