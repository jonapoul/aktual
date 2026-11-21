package aktual.updates

import aktual.budget.model.BudgetId
import aktual.core.model.CrashFile
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import aktual.prefs.AppPreferences
import aktual.prefs.NullablePreference
import aktual.prefs.Preference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

// In memory, so changes reach collectors on the test scheduler rather than a DataStore thread
internal class FakeAppPreferences(checkForNightlyUpdates: Boolean) : AppPreferences {
  override val token: NullablePreference<Token> = InMemoryNullablePreference()
  override val serverUrl: NullablePreference<ServerUrl> = InMemoryNullablePreference()
  override val mostRecentNumBudgets: Preference<Int> = InMemoryPreference(3)
  override val lastOpenedBudgetId: NullablePreference<BudgetId> = InMemoryNullablePreference()
  override val lastSeenCrash: NullablePreference<CrashFile> = InMemoryNullablePreference()
  override val checkForNightlyUpdates: Preference<Boolean> =
    InMemoryPreference(checkForNightlyUpdates)
}

private class InMemoryPreference<T : Any>(override val default: T) : Preference<T> {
  private val state = MutableStateFlow(default)

  override suspend fun get(): T = state.value

  override suspend fun set(value: T?) = state.update { value ?: default }

  override fun asFlow(): Flow<T> = state
}

private class InMemoryNullablePreference<T : Any> : NullablePreference<T> {
  private val state = MutableStateFlow<T?>(null)

  override val default: T? = null

  override suspend fun get(): T? = state.value

  override suspend fun set(value: T?) = state.update { value }

  override fun asFlow(): Flow<T?> = state
}
