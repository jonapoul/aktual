package actual.db

import actual.db.RoomBudgetDatabase.Companion.build
import android.content.Context
import androidx.room.Room
import kotlin.coroutines.CoroutineContext

fun buildDatabase(
  context: Context,
  coroutineContext: CoroutineContext,
): RoomBudgetDatabase = Room
  .databaseBuilder<RoomBudgetDatabase>(context.applicationContext, RoomBudgetDatabase.FILENAME)
  .build(coroutineContext)
