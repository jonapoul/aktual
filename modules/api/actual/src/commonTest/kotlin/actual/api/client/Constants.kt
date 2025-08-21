package actual.api.client

import actual.budget.model.BudgetId
import actual.core.model.LoginToken
import actual.core.model.Protocol
import actual.core.model.ServerUrl

val TOKEN = LoginToken("abc-123")
val BUDGET_ID = BudgetId("xyz-789")
val SERVER_URL = ServerUrl(Protocol.Https, "test.server.com")
