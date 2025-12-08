package aktual.api.client

import aktual.budget.model.BudgetId
import aktual.core.model.LoginToken
import aktual.core.model.Protocol
import aktual.core.model.ServerUrl

val TOKEN = LoginToken("abc-123")
val BUDGET_ID = BudgetId("xyz-789")
val SERVER_URL = ServerUrl(Protocol.Https, "test.server.com")
