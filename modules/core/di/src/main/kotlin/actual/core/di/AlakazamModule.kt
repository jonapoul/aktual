package actual.core.di

import alakazam.android.core.UrlOpener
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AlakazamModule {
  @Provides
  fun urlOpener(context: Context) = UrlOpener(context)
}
