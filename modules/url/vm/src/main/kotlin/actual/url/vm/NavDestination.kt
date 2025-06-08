package actual.url.vm

import androidx.compose.runtime.Immutable

@Immutable
sealed interface NavDestination {
  data object Back : NavDestination
  data object ToBootstrap : NavDestination
  data object ToLogin : NavDestination
  data object ToAbout : NavDestination
}
