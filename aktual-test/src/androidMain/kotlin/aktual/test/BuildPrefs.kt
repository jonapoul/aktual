package aktual.test

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.TestScope

fun TestScope.buildPreferences(
  coroutineContext: CoroutineContext = EmptyCoroutineContext,
  context: Context = ApplicationProvider.getApplicationContext(),
): DataStore<Preferences> =
  PreferenceDataStoreFactory.create(
    produceFile = { context.preferencesDataStoreFile("test_${System.nanoTime()}") },
    scope = this + coroutineContext,
  )
