package actual.api.client

import actual.account.model.LoginToken
import actual.budget.model.BudgetId
import actual.url.model.Protocol
import actual.url.model.ServerUrl

val TOKEN = LoginToken("abc-123")
val BUDGET_ID = BudgetId("xyz-789")
val SERVER_URL = ServerUrl(Protocol.Https, "test.unused.com")
