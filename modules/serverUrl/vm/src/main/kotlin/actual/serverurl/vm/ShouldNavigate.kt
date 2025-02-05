package actual.serverurl.vm

import androidx.compose.runtime.Immutable

@Immutable
sealed interface ShouldNavigate {
  data object ToBootstrap : ShouldNavigate
  data object ToLogin : ShouldNavigate
}
