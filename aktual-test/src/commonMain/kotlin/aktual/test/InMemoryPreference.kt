package aktual.test

import aktual.prefs.Preference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class InMemoryPreference<T : Any>(override val default: T) : Preference<T> {
  private val state = MutableStateFlow(default)

  override suspend fun get(): T = state.value

  override suspend fun set(value: T?) = state.update { value ?: default }

  override fun asFlow(): Flow<T> = state
}
