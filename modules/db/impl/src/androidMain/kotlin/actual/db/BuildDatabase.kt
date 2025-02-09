package actual.db

import actual.db.ActualDatabase.Companion.build
import android.content.Context
import androidx.room.Room
import kotlin.coroutines.CoroutineContext

fun buildDatabase(
  context: Context,
  coroutineContext: CoroutineContext,
): ActualDatabase = Room
  .databaseBuilder<ActualDatabase>(context.applicationContext, ActualDatabase.FILENAME)
  .build(coroutineContext)
