package actual.url.vm

import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface ConfirmResult {
  data class Failed(
    val reason: String,
  ) : ConfirmResult

  data class Succeeded(
    val isBootstrapped: Boolean,
  ) : ConfirmResult
}
