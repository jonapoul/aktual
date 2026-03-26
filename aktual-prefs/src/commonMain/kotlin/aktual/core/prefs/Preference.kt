package aktual.core.prefs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

interface NullablePreference<T : Any> {
  val default: T?

  suspend fun get(): T?

  suspend fun set(value: T?)

  fun asFlow(): Flow<T?>
}

interface Preference<T : Any> : NullablePreference<T> {
  override val default: T

  override suspend fun get(): T

  override fun asFlow(): Flow<T>
}

suspend fun <T : Any> NullablePreference<T>.delete() = set(null)

fun <T : Any> NullablePreference<T>.asStateFlow(
  scope: CoroutineScope,
  started: SharingStarted = SharingStarted.Eagerly,
): StateFlow<T?> = asFlow().stateIn(scope, started, default)

fun <T : Any> Preference<T>.asStateFlow(
  scope: CoroutineScope,
  started: SharingStarted = SharingStarted.Eagerly,
): StateFlow<T> = asFlow().stateIn(scope, started, default)

fun <T : Any> NullablePreference<T>.required(): Preference<T> {
  val parent = this
  return object : Preference<T>, NullablePreference<T> by parent {
    override val default: T = parent.default ?: error("Must use a non-null default for $parent")

    override suspend fun get(): T = parent.get() ?: default

    override fun asFlow(): Flow<T> = parent.asFlow().map { it ?: default }.distinctUntilChanged()
  }
}
