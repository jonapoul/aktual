package aktual.prefs.vm

import aktual.prefs.Preference
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Composable
internal fun <T : Any> ViewModel.collectAsState(preference: Preference<T>): State<T> {
  // Seed with the stored value, not the default, so toggles don't animate in on first composition
  val stateFlow =
    remember(preference) {
      val initial = runBlocking { preference.get() }
      preference.asFlow().stateIn(viewModelScope, SharingStarted.Eagerly, initial)
    }
  return stateFlow.collectAsState()
}

internal fun <T : Any> ViewModel.launchAndSet(preference: Preference<T>, value: T) {
  viewModelScope.launch { preference.set(value) }
}
