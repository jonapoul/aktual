package actual.about.ui.info

import androidx.compose.runtime.Immutable

@Immutable
interface InfoNavigator {
  fun back(): Boolean
  fun toLicenses()
}
