package actual.android.app

import actual.core.ui.ActualColorSchemeType
import dev.jonpoulton.preferences.core.Preferences
import timber.log.Timber
import javax.inject.Inject

internal class ColorSchemePreferences @Inject constructor(
  private val prefs: Preferences,
) {
  val colorSchemeType: ActualColorSchemeType
    get() = prefs
      .getInt(key = "colorSchemeType", default = -1)
      .get()
      .let { int -> if (int == -1) ActualColorSchemeType.System else ActualColorSchemeType.entries[int] }
      .also { type -> Timber.v("Got ActualColorSchemeType %s from preferences", type) }
}
