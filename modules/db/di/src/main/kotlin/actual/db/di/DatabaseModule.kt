package actual.db.di

import actual.core.coroutines.CoroutineContexts
import actual.db.ActualDatabase
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
  ): ActualDatabase = buildDatabase(context, contexts.io)
}
