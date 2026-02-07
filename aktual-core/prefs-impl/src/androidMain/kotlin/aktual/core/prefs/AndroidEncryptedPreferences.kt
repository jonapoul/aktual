package aktual.core.prefs

import android.content.SharedPreferences
import dev.jonpoulton.preferences.android.AndroidSharedPreferences
import dev.jonpoulton.preferences.core.Preferences
import kotlin.coroutines.CoroutineContext

class AndroidEncryptedPreferences(
  prefs: SharedPreferences,
  context: CoroutineContext,
) : EncryptedPreferences, Preferences by AndroidSharedPreferences(prefs, context)
