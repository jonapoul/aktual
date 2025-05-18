package actual.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
class ContextModule {
  @Provides
  @Singleton
  fun context(
    @ApplicationContext app: Context,
  ): Context = app
}
