package aktual.updates.vm

import androidx.compose.runtime.Immutable

@Immutable
sealed interface NightlyUpdateState {
  data object None : NightlyUpdateState

  data class Available(
    val installed: Version,
    val nightly: Version,
    val commitsAhead: Int,
    val apkUrl: String,
  ) : NightlyUpdateState
}

@Immutable data class Version(val name: String, val gitHash: String)
