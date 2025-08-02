package actual.di

import alakazam.android.core.UrlOpener
import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

@BindingContainer
@ContributesTo(AppScope::class)
object AndroidAlakazamContainer {
  @Provides
  fun urlOpener(context: Context): UrlOpener = UrlOpener(context)
}
