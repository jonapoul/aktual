package aktual.test

import aktual.budget.model.AccountId
import aktual.budget.model.AccountSpec
import aktual.budget.model.BudgetId
import aktual.budget.model.DashboardPageId
import aktual.budget.model.DbMetadata
import aktual.budget.model.RuleId
import aktual.budget.model.TransactionsSpec
import aktual.budget.model.WidgetId
import aktual.core.model.ServerUrl
import aktual.core.model.Token

internal val SERVER_URL = ServerUrl("https://website.com")

internal val LOGIN_TOKEN = Token("abc-123")

internal val BUDGET_ID = BudgetId("abc-123")

internal val ACCOUNT_ID = AccountId("abc-123")

internal val TRANSACTIONS_SPEC = TransactionsSpec(AccountSpec.AllAccounts)

internal val DB_METADATA = DbMetadata(budgetName = "My Budget", cloudFileId = BUDGET_ID)

internal val SECOND_BUDGET_ID = BudgetId("def-456")

internal val SECOND_DB_METADATA =
  DbMetadata(budgetName = "Other Budget", cloudFileId = SECOND_BUDGET_ID)

internal val DASHBOARD_PAGE_ID = DashboardPageId("page")

internal val WIDGET_ID = WidgetId("widget")

internal val RULE_ID = RuleId("abc-123")
