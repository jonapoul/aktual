package aktual.updates.vm

import androidx.compose.runtime.Immutable

@Immutable
sealed interface NightlyUpdateState {
  data object None : NightlyUpdateState

  data class Available(
    val installed: Version,
    val nightly: Version,
    val commitsAhead: Int,
    val install: InstallState,
  ) : NightlyUpdateState
}

@Immutable data class Version(val name: String, val gitHash: String)

@Immutable
sealed interface InstallState {
  data object Idle : InstallState

  // progress is between 0 and 1, or null when the size isn't known yet
  @JvmInline value class Downloading(val progress: Float?) : InstallState

  data object Failed : InstallState
}
