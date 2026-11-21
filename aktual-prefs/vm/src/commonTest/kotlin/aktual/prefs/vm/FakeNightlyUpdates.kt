package aktual.prefs.vm

import aktual.updates.NightlyBuild
import aktual.updates.NightlyUpdates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class FakeNightlyUpdates(override val isSupported: Boolean = false) : NightlyUpdates {
  override val latest: StateFlow<NightlyBuild?> = MutableStateFlow(null)
}
