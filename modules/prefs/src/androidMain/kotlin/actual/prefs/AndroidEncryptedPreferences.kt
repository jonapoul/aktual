package actual.prefs

import alakazam.kotlin.core.CoroutineContexts
import android.content.SharedPreferences
import dev.jonpoulton.preferences.android.AndroidSharedPreferences
import dev.jonpoulton.preferences.core.Preferences

class AndroidEncryptedPreferences(
  prefs: SharedPreferences,
  contexts: CoroutineContexts,
) : EncryptedPreferences, Preferences by AndroidSharedPreferences(prefs, contexts.io)
