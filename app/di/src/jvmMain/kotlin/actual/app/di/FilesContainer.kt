package actual.app.di

import actual.core.model.Files
import actual.core.model.SystemFiles
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

@BindingContainer
@ContributesTo(AppScope::class)
object FilesContainer {
  @Provides
  fun prefs(): Files = SystemFiles
}
