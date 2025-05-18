package actual.about.info.ui

import androidx.compose.runtime.Immutable

@Immutable
interface InfoNavigator {
  fun back(): Boolean
  fun toLicenses()
}
