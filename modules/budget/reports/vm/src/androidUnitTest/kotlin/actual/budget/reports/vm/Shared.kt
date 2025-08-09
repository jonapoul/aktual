package actual.budget.reports.vm

import actual.budget.model.BudgetId
import actual.budget.model.DbMetadata
import kotlinx.collections.immutable.persistentMapOf

val TEST_BUDGET_ID = BudgetId("b08c6deb-94b8-44ae-bebf-a47e18827df0")

internal val TEST_METADATA = DbMetadata(
  data = persistentMapOf(
    "cloudFileId" to TEST_BUDGET_ID.value,
  ),
)
