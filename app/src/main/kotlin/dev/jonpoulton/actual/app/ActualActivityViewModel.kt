package dev.jonpoulton.actual.app

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jonpoulton.actual.core.connection.ConnectionMonitor
import dev.jonpoulton.actual.core.ui.ActualColorSchemeType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
internal class ActualActivityViewModel @Inject constructor(
  private val scope: CoroutineScope,
  private val connectionMonitor: ConnectionMonitor,
  private val colorSchemePrefs: ColorSchemePreferences,
) : ViewModel() {
  val colorSchemeType: ActualColorSchemeType
    get() = colorSchemePrefs.colorSchemeType

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
