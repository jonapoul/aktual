package aktual.prefs.vm

import aktual.prefs.Preference
import aktual.prefs.asStateFlow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

@Composable
internal fun <T : Any> ViewModel.collectAsState(preference: Preference<T>): State<T> {
  val stateFlow = remember(preference) { preference.asStateFlow(viewModelScope) }
  return stateFlow.collectAsState()
}

internal fun <T : Any> ViewModel.launchAndSet(preference: Preference<T>, value: T) {
  viewModelScope.launch { preference.set(value) }
}
