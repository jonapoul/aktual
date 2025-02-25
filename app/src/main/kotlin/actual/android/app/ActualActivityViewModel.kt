package actual.android.app

import actual.account.login.domain.LoginPreferences
import actual.account.model.LoginToken
import actual.core.colorscheme.ColorSchemePreferences
import actual.core.colorscheme.ColorSchemeType
import actual.core.connection.ConnectionMonitor
import actual.core.connection.ServerVersionFetcher
import actual.log.Logger
import actual.url.prefs.ServerUrlPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ActualActivityViewModel @Inject constructor(
  private val scope: CoroutineScope,
  private val connectionMonitor: ConnectionMonitor,
  private val serverVersionFetcher: ServerVersionFetcher,
  colorSchemePrefs: ColorSchemePreferences,
  serverUrlPreferences: ServerUrlPreferences,
  loginPreferences: LoginPreferences,
) : ViewModel() {
  val colorSchemeType: StateFlow<ColorSchemeType> = colorSchemePrefs.stateFlow(viewModelScope)

  val isServerUrlSet: Boolean = serverUrlPreferences.url.isSet()

  val loginToken: LoginToken? = loginPreferences.token.get()

  fun start() {
    connectionMonitor.start()
    viewModelScope.launch {
      serverVersionFetcher.startFetching()
    }
  }

  override fun onCleared() {
    Logger.v("onCleared")
    super.onCleared()
    connectionMonitor.stop()
    scope.cancel()
  }
}
