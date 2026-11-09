package aktual.core.ui

import androidx.compose.runtime.Immutable

@Immutable
fun interface OnAction<in A> {
  operator fun invoke(action: A)
}
