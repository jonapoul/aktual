package actual.db

import actual.db.BudgetDatabase.Companion.build
import android.content.Context
import androidx.room.Room
import kotlin.coroutines.CoroutineContext

fun buildDatabase(
  context: Context,
  coroutineContext: CoroutineContext,
): BudgetDatabase = Room
  .databaseBuilder<BudgetDatabase>(context.applicationContext, BudgetDatabase.FILENAME)
  .build(coroutineContext)
