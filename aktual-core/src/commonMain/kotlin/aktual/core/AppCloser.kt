package aktual.core

import androidx.compose.runtime.Immutable

@Immutable
fun interface AppCloser {
  operator fun invoke()
}
