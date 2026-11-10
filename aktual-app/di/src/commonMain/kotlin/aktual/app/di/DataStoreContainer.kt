package aktual.app.di

import aktual.core.utils.AppDirectory
import aktual.di.AppCoroutineScope
import aktual.di.AppScope
import aktual.log
import aktual.prefs.migration.preferenceMigrations
import alakazam.kotlin.CoroutineContexts
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.plus

@BindingContainer
@ContributesTo(AppScope::class)
object DataStoreContainer {
  @Provides
  @SingleIn(AppScope::class)
  fun dataStore(
    appDirectory: AppDirectory,
    scope: AppCoroutineScope,
    contexts: CoroutineContexts,
  ): DataStore<Preferences> {
    val dir = appDirectory.get().toFile()
    dir.mkdirs()
    return PreferenceDataStoreFactory.create(
      corruptionHandler =
        ReplaceFileCorruptionHandler { e ->
          log.e(e) { "Settings file is corrupted, resetting to defaults" }
          emptyPreferences()
        },
      produceFile = { dir.resolve("settings.preferences_pb") },
      scope = scope + contexts.io,
      migrations = preferenceMigrations(),
    )
  }
}
