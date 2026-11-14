package aktual.budget.db

import aktual.budget.model.BudgetId
import app.cash.sqldelight.db.SqlDriver

fun interface SqlDriverFactory {
  fun create(budgetId: BudgetId): SqlDriver
}
