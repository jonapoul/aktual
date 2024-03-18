package dev.jonpoulton.actual.serverurl.vm

import androidx.compose.runtime.Immutable

@Immutable
sealed interface ShouldNavigate {
  @Immutable
  data object ToBootstrap : ShouldNavigate

  @Immutable
  data object ToLogin : ShouldNavigate
}
