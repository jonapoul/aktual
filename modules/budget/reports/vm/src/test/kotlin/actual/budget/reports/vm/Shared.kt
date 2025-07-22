package actual.budget.reports.vm

import actual.account.model.LoginToken
import actual.budget.model.BudgetId
import actual.budget.model.DbMetadata
import kotlinx.collections.immutable.persistentMapOf
import kotlin.uuid.Uuid

val TEST_UUID = Uuid.parse("cb611070-fc19-43f4-96e3-f349152d3da1")
val TEST_TOKEN = LoginToken("abc-123")
val TEST_BUDGET_ID = BudgetId("b08c6deb-94b8-44ae-bebf-a47e18827df0")

internal val TEST_METADATA = DbMetadata(
  data = persistentMapOf(
    "cloudFileId" to TEST_BUDGET_ID.value,
  ),
)
