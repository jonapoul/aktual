package aktual.test

import aktual.budget.model.AccountSpec
import aktual.budget.model.BudgetId
import aktual.budget.model.DbMetadata
import aktual.budget.model.TransactionsSpec
import aktual.core.model.ServerUrl
import aktual.core.model.Token

internal val SERVER_URL = ServerUrl("https://website.com")

internal val LOGIN_TOKEN = Token("abc-123")

internal val BUDGET_ID = BudgetId("abc-123")

internal val TRANSACTIONS_SPEC = TransactionsSpec(AccountSpec.AllAccounts)

internal val DB_METADATA = DbMetadata(budgetName = "My Budget", cloudFileId = BUDGET_ID)

internal val SECOND_BUDGET_ID = BudgetId("def-456")

internal val SECOND_DB_METADATA =
  DbMetadata(budgetName = "Other Budget", cloudFileId = SECOND_BUDGET_ID)
