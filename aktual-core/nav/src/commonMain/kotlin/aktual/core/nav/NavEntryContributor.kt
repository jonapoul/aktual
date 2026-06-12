package aktual.core.nav

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

/** Contributes nav entries from a feature module into the shared [EntryProviderScope]. */
@Immutable
fun interface NavEntryContributor {
  fun EntryProviderScope<NavKey>.contribute(stack: NavStack<NavKey>)
}
