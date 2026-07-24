package aktual.budget

import aktual.budget.model.SyncState
import aktual.di.AppScope
import alakazam.kotlin.StateHolder
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject @SingleIn(AppScope::class) class SyncStateHolder : StateHolder<SyncState>(Inactive)
