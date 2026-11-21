package aktual.updates

import kotlinx.coroutines.flow.StateFlow

interface NightlyUpdates {
  // Only Android release builds
  val isSupported: Boolean

  // Null until a newer nightly is found. Checks once per launch while the setting is on, and again
  // whenever it's turned back on
  val latest: StateFlow<NightlyBuild?>
}
