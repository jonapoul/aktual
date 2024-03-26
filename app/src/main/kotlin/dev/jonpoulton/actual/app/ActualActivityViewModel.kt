package dev.jonpoulton.actual.app

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jonpoulton.actual.core.connection.ConnectionMonitor
import dev.jonpoulton.actual.core.ui.ActualColorSchemeType
import dev.jonpoulton.actual.serverurl.prefs.ServerUrlPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
internal class ActualActivityViewModel @Inject constructor(
  private val scope: CoroutineScope,
  private val connectionMonitor: ConnectionMonitor,
  colorSchemePrefs: ColorSchemePreferences,
  serverUrlPreferences: ServerUrlPreferences,
) : ViewModel() {
  val colorSchemeType: ActualColorSchemeType = colorSchemePrefs.colorSchemeType
  val isServerUrlSet: Boolean = serverUrlPreferences.url.isSet()

  fun start() {
    connectionMonitor.start()
  }

  override fun onCleared() {
    Timber.v("onCleared")
    super.onCleared()
    connectionMonitor.stop()
    scope.cancel()
  }
}
