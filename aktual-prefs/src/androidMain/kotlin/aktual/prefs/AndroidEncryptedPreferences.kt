package aktual.prefs

import android.content.SharedPreferences
import dev.jonpoulton.preferences.android.AndroidSharedPreferences
import dev.jonpoulton.preferences.core.Preferences
import kotlin.coroutines.CoroutineContext

class AndroidEncryptedPreferences(
  prefs: SharedPreferences,
  io: CoroutineContext,
) : EncryptedPreferences, Preferences by AndroidSharedPreferences(prefs, io)
