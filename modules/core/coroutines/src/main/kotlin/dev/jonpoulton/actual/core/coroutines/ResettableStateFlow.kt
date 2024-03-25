package dev.jonpoulton.actual.core.coroutines

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

interface ResettableStateFlow<T> : MutableStateFlow<T> {
  fun reset()
}

fun <T> ResettableStateFlow(value: T): ResettableStateFlow<T> = ResettableStateFlowImpl(value)

internal class ResettableStateFlowImpl<T>(
  private val initialValue: T,
) : ResettableStateFlow<T>, MutableStateFlow<T> by MutableStateFlow(initialValue) {
  override fun reset() = update { initialValue }
}
