package actual.app.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import java.util.prefs.Preferences

@BindingContainer
@ContributesTo(AppScope::class)
object JvmPreferencesContainer {
  @Provides
  fun prefs(): Preferences = Preferences.userRoot().node("actual")
}
