@file:Suppress("DEPRECATION")

package actual.di.app

import actual.prefs.AndroidEncryptedPreferences
import actual.prefs.EncryptedPreferences
import alakazam.kotlin.core.CoroutineContexts
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV
import androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
import androidx.security.crypto.MasterKey
import androidx.security.crypto.MasterKey.KeyScheme
import dev.jonpoulton.preferences.android.AndroidSharedPreferences
import dev.jonpoulton.preferences.core.Preferences
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

@BindingContainer
@ContributesTo(AppScope::class)
object AndroidPreferencesContainer {
  @Provides
  fun sharedPrefs(
    context: Context,
  ): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

  @Provides
  fun prefs(
    prefs: SharedPreferences,
    contexts: CoroutineContexts,
  ): Preferences = AndroidSharedPreferences(prefs, contexts.io)

  @Provides
  fun encrypted(
    context: Context,
    contexts: CoroutineContexts,
  ): EncryptedPreferences {
    val masterKey = MasterKey
      .Builder(context)
      .setKeyScheme(KeyScheme.AES256_GCM)
      .build()
    val prefs = EncryptedSharedPreferences.create(context, "encrypted-prefs", masterKey, AES256_SIV, AES256_GCM)
    return AndroidEncryptedPreferences(prefs, contexts.io)
  }
}
