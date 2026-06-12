package aktual.budget.navrail.ui.edit

import aktual.core.nav.BackNavigator
import aktual.core.nav.EditNavGridNavRoute
import aktual.core.nav.NavEntryContributor
import aktual.core.nav.NavStack
import aktual.di.AppScope
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

// EditNavGrid is a standalone, app-level screen — pushed onto the root stack, not embedded in the
// budget nav rail. It is therefore registered by its own contributor rather than the nav rail's
@ContributesIntoSet(AppScope::class)
class EditNavGridNavEntryContributor : NavEntryContributor {
  override fun EntryProviderScope<NavKey>.contribute(stack: NavStack<NavKey>) {
    entry<EditNavGridNavRoute> { EditNavGridScreen(back = BackNavigator(stack)) }
  }
}
