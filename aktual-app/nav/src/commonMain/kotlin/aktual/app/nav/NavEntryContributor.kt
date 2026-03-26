package aktual.app.nav

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

/** Contributes nav entries from a feature module into the shared [EntryProviderScope]. */
@Immutable
fun interface NavEntryContributor {
  fun contribute(scope: EntryProviderScope<NavKey>, stack: SnapshotStateList<NavKey>)
}
