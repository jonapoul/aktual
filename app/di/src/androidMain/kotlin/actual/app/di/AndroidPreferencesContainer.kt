/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("DEPRECATION")

package actual.app.di

import actual.prefs.AndroidEncryptedPreferences
import actual.prefs.EncryptedPreferences
import alakazam.kotlin.core.CoroutineContexts
import android.content.Context
import android.content.SharedPreferences
import android.os.StrictMode
import android.preference.PreferenceManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme
import androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme
import androidx.security.crypto.MasterKey
import dev.jonpoulton.preferences.android.AndroidSharedPreferences
import dev.jonpoulton.preferences.core.Preferences
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
@ContributesTo(AppScope::class)
object AndroidPreferencesContainer {
  @Provides
  fun prefs(
    prefs: SharedPreferences,
    contexts: CoroutineContexts,
  ): Preferences = AndroidSharedPreferences(prefs, contexts.io)

  @Provides
  fun sharedPrefs(
    context: Context,
  ): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

  // This takes time to initialize, so allow disk reads on the main thread (but only for this)
  // https://developer.android.com/reference/android/os/StrictMode.ThreadPolicy.Builder#permitDisk
  @Provides
  @SingleIn(AppScope::class)
  fun encrypted(
    context: Context,
    contexts: CoroutineContexts,
  ): EncryptedPreferences = allowDiskAndSlowCalls {
    val masterKey = MasterKey
      .Builder(context)
      .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
      .build()
    val prefs = EncryptedSharedPreferences.create(
      context,
      "encrypted-prefs",
      masterKey,
      PrefKeyEncryptionScheme.AES256_SIV,
      PrefValueEncryptionScheme.AES256_GCM,
    )
    AndroidEncryptedPreferences(prefs, contexts.io)
  }
}

private fun <T> allowDiskAndSlowCalls(block: () -> T): T {
  val old = StrictMode.getThreadPolicy()
  StrictMode.setThreadPolicy(
    StrictMode.ThreadPolicy
      .Builder(old)
      .permitDiskReads()
      .permitDiskWrites()
      .permitCustomSlowCalls()
      .build(),
  )
  return try {
    block()
  } finally {
    StrictMode.setThreadPolicy(old)
  }
}
