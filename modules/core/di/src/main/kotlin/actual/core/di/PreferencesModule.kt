@file:Suppress("DEPRECATION")

package actual.core.di

import actual.prefs.AndroidEncryptedPreferences
import actual.prefs.EncryptedPreferences
import alakazam.kotlin.core.CoroutineContexts
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme
import androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme
import androidx.security.crypto.MasterKey
import androidx.security.crypto.MasterKey.KeyScheme
import dagger.Module
import dagger.Provides
import dev.jonpoulton.preferences.android.AndroidSharedPreferences
import dev.jonpoulton.preferences.core.Preferences
import javax.inject.Singleton

@Module
class PreferencesModule {
  @Provides
  @Singleton
  fun prefs(
    prefs: SharedPreferences,
    contexts: CoroutineContexts,
  ): Preferences = AndroidSharedPreferences(prefs, contexts.io)

  @Provides
  @Singleton
  fun sharedPrefs(
    context: Context,
  ): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

  @Provides
  @Singleton
  fun encrypted(
    context: Context,
    contexts: CoroutineContexts,
  ): EncryptedPreferences {
    val masterKey = MasterKey
      .Builder(context)
      .setKeyScheme(KeyScheme.AES256_GCM)
      .build()

    val prefs = EncryptedSharedPreferences.create(
      context,
      "encrypted-prefs",
      masterKey,
      PrefKeyEncryptionScheme.AES256_SIV,
      PrefValueEncryptionScheme.AES256_GCM,
    )

    return AndroidEncryptedPreferences(prefs, contexts.io)
  }
}
