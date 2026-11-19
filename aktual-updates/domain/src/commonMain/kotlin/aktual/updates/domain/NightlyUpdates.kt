package aktual.updates.domain

import kotlinx.coroutines.flow.StateFlow

interface NightlyUpdates {
  // Only release builds on platforms that can install an APK
  val isSupported: Boolean

  // Null until a newer nightly is found. Checks once per launch while the setting is on, and again
  // whenever it's turned back on
  val latest: StateFlow<NightlyBuild?>
}
