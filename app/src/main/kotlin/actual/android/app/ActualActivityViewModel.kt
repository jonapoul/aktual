package actual.android.app

import actual.core.connection.ConnectionMonitor
import actual.core.connection.ServerVersionFetcher
import actual.core.ui.ActualColorSchemeType
import actual.serverurl.prefs.ServerUrlPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
internal class ActualActivityViewModel @Inject constructor(
  private val scope: CoroutineScope,
  private val connectionMonitor: ConnectionMonitor,
  private val serverVersionFetcher: ServerVersionFetcher,
  colorSchemePrefs: ColorSchemePreferences,
  serverUrlPreferences: ServerUrlPreferences,
) : ViewModel() {
  val colorSchemeType: ActualColorSchemeType = colorSchemePrefs.colorSchemeType
  val isServerUrlSet: Boolean = serverUrlPreferences.url.isSet()

  fun start() {
    connectionMonitor.start()
    viewModelScope.launch {
      serverVersionFetcher.startFetching()
    }
  }

  override fun onCleared() {
    Timber.v("onCleared")
    super.onCleared()
    connectionMonitor.stop()
    scope.cancel()
  }
}
