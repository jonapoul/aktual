package actual.url.vm

import androidx.compose.runtime.Immutable
import dev.drewhamilton.poko.Poko

@Immutable
internal sealed interface ConfirmResult {
  @Poko class Failed(
    val reason: String,
  ) : ConfirmResult

  @Poko class Succeeded(
    val isBootstrapped: Boolean,
  ) : ConfirmResult
}
