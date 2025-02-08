package actual.core.ui

import androidx.compose.runtime.MutableState

fun <T> MutableState<T>.set(value: T) {
  this.value = value
}
