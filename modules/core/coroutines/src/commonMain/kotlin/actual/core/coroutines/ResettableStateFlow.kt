package actual.core.coroutines

import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalForInheritanceCoroutinesApi::class, ExperimentalSubclassOptIn::class)
interface ResettableStateFlow<T> : MutableStateFlow<T> {
  fun reset()
}

fun <T> ResettableStateFlow(value: T): ResettableStateFlow<T> = ResettableStateFlowImpl(value)

private class ResettableStateFlowImpl<T>(
  private val initialValue: T,
) : ResettableStateFlow<T>, MutableStateFlow<T> by MutableStateFlow(initialValue) {
  override fun reset() = update { initialValue }
}
