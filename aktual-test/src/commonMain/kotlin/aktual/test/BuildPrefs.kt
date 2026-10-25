package aktual.test

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import java.nio.file.Files
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.TestScope

fun TestScope.buildPreferences(
  coroutineContext: CoroutineContext = EmptyCoroutineContext,
): DataStore<Preferences> {
  val dir = Files.createTempDirectory("prefs").toFile().apply { deleteOnExit() }
  return PreferenceDataStoreFactory.create(
    produceFile = { dir.resolve("test.preferences_pb").apply { deleteOnExit() } },
    scope = this + coroutineContext,
  )
}
