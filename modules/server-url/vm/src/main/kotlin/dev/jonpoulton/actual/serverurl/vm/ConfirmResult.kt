package dev.jonpoulton.actual.serverurl.vm

import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface ConfirmResult {
  @Immutable
  data class Failed(val reason: String) : ConfirmResult

  @Immutable
  data object Succeeded : ConfirmResult
}
