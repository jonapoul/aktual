package aktual.test

import aktual.core.prefs.AndroidEncryptedPreferences
import aktual.core.prefs.EncryptedPreferences
import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import dev.jonpoulton.preferences.android.AndroidSharedPreferences
import dev.jonpoulton.preferences.core.Preferences
import kotlin.coroutines.CoroutineContext

fun buildSharedPreferences(
  context: Context = ApplicationProvider.getApplicationContext(),
  name: String = "prefs",
): SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)

fun buildPreferences(
  coroutineContext: CoroutineContext,
  sharedPreferences: SharedPreferences = buildSharedPreferences(),
): Preferences = AndroidSharedPreferences(sharedPreferences, coroutineContext)

fun buildEncryptedPreferences(
  coroutineContext: CoroutineContext,
  sharedPreferences: SharedPreferences = buildSharedPreferences(),
): EncryptedPreferences = AndroidEncryptedPreferences(sharedPreferences, coroutineContext)
