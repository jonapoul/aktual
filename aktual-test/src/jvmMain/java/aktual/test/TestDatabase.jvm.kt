package aktual.test

import aktual.budget.db.BudgetDatabase
import androidx.room3.Room
import androidx.room3.RoomDatabase

internal actual fun inMemoryDatabaseBuilder(): RoomDatabase.Builder<BudgetDatabase> =
  Room.inMemoryDatabaseBuilder()
