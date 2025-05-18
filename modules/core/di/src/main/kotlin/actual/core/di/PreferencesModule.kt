@file:Suppress("DEPRECATION")

package actual.core.di

import alakazam.kotlin.core.CoroutineContexts
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
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
  fun sharedPrefs(
    context: Context,
  ): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
}
