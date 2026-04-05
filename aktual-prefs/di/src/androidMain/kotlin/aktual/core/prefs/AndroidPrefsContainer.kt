package aktual.core.prefs

import alakazam.kotlin.CoroutineContexts
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus

@BindingContainer
@ContributesTo(AppScope::class)
object AndroidPrefsContainer {
  @Provides
  @SingleIn(AppScope::class)
  fun dataStore(
    context: Context,
    scope: CoroutineScope,
    contexts: CoroutineContexts,
  ): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create(
      produceFile = { context.preferencesDataStoreFile("settings") },
      scope = scope + contexts.io,
      migrations = emptyList(),
    )
  }
}
