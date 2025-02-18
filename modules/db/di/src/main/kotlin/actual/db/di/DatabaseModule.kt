package actual.db.di

import actual.core.coroutines.CoroutineContexts
import actual.db.RoomBudgetDatabase
import actual.db.buildDatabase
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {
  @Provides
  @Singleton
  fun db(
    context: Context,
    contexts: CoroutineContexts,
  ): RoomBudgetDatabase = buildDatabase(context, contexts.io)
}
