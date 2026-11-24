package aktual.core.utils

import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * A [StateFlow] view of this one, needing no scope or initial value. [transform] runs on every
 * [StateFlow.value] read and every upstream emission, so keep it cheap and pure.
 */
fun <T, R> StateFlow<T>.mapState(transform: (T) -> R): StateFlow<R> =
  MappedStateFlow(this, transform)

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
private class MappedStateFlow<T, R>(
  private val source: StateFlow<T>,
  private val transform: (T) -> R,
) : StateFlow<R> {
  override val value: R
    get() = transform(source.value)

  override val replayCache: List<R>
    get() = listOf(value)

  override suspend fun collect(collector: FlowCollector<R>): Nothing {
    source.map(transform).distinctUntilChanged().collect(collector)
    awaitCancellation()
  }
}
