package actual.db.di

import actual.core.coroutines.CoroutineContexts
import actual.db.BudgetDatabase
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
  ): BudgetDatabase = buildDatabase(context, contexts.io)
}
