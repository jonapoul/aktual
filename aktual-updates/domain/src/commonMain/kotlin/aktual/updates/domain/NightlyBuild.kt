package aktual.updates.domain

import kotlin.time.Instant

// The newest successful nightly run on main, when it's ahead of the installed commit
data class NightlyBuild(
  val gitHash: String,
  val commitTime: Instant,
  val commitsAhead: Int,
  val apkUrl: String,
)
