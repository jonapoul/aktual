package aktual.prefs.vm

import aktual.prefs.NullablePreference
import aktual.prefs.Preference
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Composable
internal fun <T : Any> ViewModel.collectAsState(preference: Preference<T>): State<T> {
  val stateFlow = remember(preference) { preference.asLoadedStateFlow(viewModelScope) }
  return stateFlow.collectAsState()
}

// Seeded with the stored value, not the default, so switches don't animate on first load
internal fun <T : Any> Preference<T>.asLoadedStateFlow(scope: CoroutineScope): StateFlow<T> =
  asFlow().stateIn(scope, Eagerly, runBlocking { get() })

internal fun <T : Any> ViewModel.launchAndSet(preference: Preference<T>, value: T) {
  viewModelScope.launch { preference.set(value) }
}

internal fun <T : Any> NullablePreference<T>.asLoadedStateFlow(
  scope: CoroutineScope,
): StateFlow<T?> = asFlow().stateIn(scope, Eagerly, runBlocking { get() })
