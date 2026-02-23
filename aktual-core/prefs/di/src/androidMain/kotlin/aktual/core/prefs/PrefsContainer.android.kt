@file:Suppress("DEPRECATION")

package aktual.core.prefs

import alakazam.kotlin.CoroutineContexts
import android.content.Context
import android.content.SharedPreferences
import android.os.StrictMode
import androidx.preference.PreferenceManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme
import androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme
import androidx.security.crypto.MasterKey
import androidx.security.crypto.MasterKey.KeyScheme
import dev.jonpoulton.preferences.android.AndroidSharedPreferences
import dev.jonpoulton.preferences.core.Preferences
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer(includes = [ProvidesPrefsContainer::class])
@ContributesTo(AppScope::class)
actual interface PrefsContainer {
  // TBC
}

@BindingContainer
@ContributesTo(AppScope::class)
private object ProvidesPrefsContainer {
  @Provides
  fun prefs(prefs: SharedPreferences, contexts: CoroutineContexts): Preferences =
    AndroidSharedPreferences(prefs, contexts.io)

  @Provides
  fun sharedPrefs(context: Context): SharedPreferences =
    PreferenceManager.getDefaultSharedPreferences(context)

  // This takes time to initialize, so allow disk reads on the main thread (but only for this)
  // https://developer.android.com/reference/android/os/StrictMode.ThreadPolicy.Builder#permitDisk
  @Provides
  @SingleIn(AppScope::class)
  fun encrypted(context: Context, contexts: CoroutineContexts): EncryptedPreferences =
    allowDiskAndSlowCalls {
      val masterKey = MasterKey.Builder(context).setKeyScheme(KeyScheme.AES256_GCM).build()
      val prefs =
        EncryptedSharedPreferences.create(
          context,
          "encrypted-prefs",
          masterKey,
          PrefKeyEncryptionScheme.AES256_SIV,
          PrefValueEncryptionScheme.AES256_GCM,
        )
      AndroidEncryptedPreferences(prefs, contexts.io)
    }

  private fun <T> allowDiskAndSlowCalls(block: () -> T): T {
    val old = StrictMode.getThreadPolicy()
    StrictMode.setThreadPolicy(
      StrictMode.ThreadPolicy.Builder(old)
        .permitDiskReads()
        .permitDiskWrites()
        .permitCustomSlowCalls()
        .build()
    )
    return try {
      block()
    } finally {
      StrictMode.setThreadPolicy(old)
    }
  }
}
