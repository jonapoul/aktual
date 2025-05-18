package actual.core.di

import alakazam.android.core.UrlOpener
import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class AlakazamModule {
  @Provides
  fun urlOpener(context: Context) = UrlOpener(context)
}
