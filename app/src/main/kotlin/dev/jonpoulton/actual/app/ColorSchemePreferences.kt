package dev.jonpoulton.actual.app

import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import dev.jonpoulton.actual.core.ui.ActualColorSchemeType
import timber.log.Timber
import javax.inject.Inject

internal class ColorSchemePreferences @Inject constructor(private val prefs: FlowSharedPreferences) {
  val colorSchemeType: ActualColorSchemeType
    get() = prefs.getInt(key = "colorSchemeType", defaultValue = -1).get()
      .let { int -> if (int == -1) ActualColorSchemeType.System else ActualColorSchemeType.entries[int] }
      .also { Timber.v("Got ActualColorSchemeType $it from preferences") }
}
