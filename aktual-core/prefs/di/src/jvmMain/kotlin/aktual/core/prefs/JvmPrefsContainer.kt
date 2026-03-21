package aktual.core.prefs

import aktual.core.model.AppDirectory
import alakazam.kotlin.CoroutineContexts
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus

@BindingContainer
@ContributesTo(AppScope::class)
object JvmPrefsContainer {
  @Provides
  @SingleIn(AppScope::class)
  fun dataStore(
    appDirectory: AppDirectory,
    scope: CoroutineScope,
    contexts: CoroutineContexts,
  ): DataStore<Preferences> {
    val dir = appDirectory.get().toFile()
    dir.mkdirs()
    return PreferenceDataStoreFactory.create(
      produceFile = { dir.resolve("settings.preferences_pb") },
      scope = scope + contexts.io,
    )
  }
}
