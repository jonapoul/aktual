package dev.jonpoulton.actual.app

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ActualActivityViewModel @Inject constructor(
  private val scope: CoroutineScope,
) : ViewModel() {
  fun start() {
    connectionMonitor.start()
  }

  override fun onCleared() {
    Timber.v("onCleared")
    super.onCleared()
    scope.cancel()
  }
}
