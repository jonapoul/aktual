package aktual.budget.schedules.domain

import kotlinx.coroutines.flow.StateFlow

// Schedules that were due to post their transactions after a sync, but didn't because the sync
// failed. Upstream's schedules-offline event
interface UnpostedSchedules {
  val hasUnposted: StateFlow<Boolean>

  // Posts them anyway, which risks duplicates if another device already has
  fun post()

  // Stops asking until the budget is opened again
  fun dismiss()
}
