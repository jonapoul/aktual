package aktual.budget.db

import androidx.room3.RoomDatabaseConstructor

internal expect object BudgetDatabaseConstructor : RoomDatabaseConstructor<BudgetDatabase> {
  override fun initialize(): BudgetDatabase
}
