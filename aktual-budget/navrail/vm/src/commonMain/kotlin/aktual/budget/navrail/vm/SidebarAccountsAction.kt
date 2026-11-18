package aktual.budget.navrail.vm

import androidx.compose.runtime.Immutable
import kotlin.jvm.JvmInline

@Immutable
sealed interface SidebarAccountsAction {
  @JvmInline value class ToggleSection(val section: SidebarSection) : SidebarAccountsAction

  data object ToggleAllSections : SidebarAccountsAction

  // Null hides the search field
  @JvmInline value class Search(val query: String?) : SidebarAccountsAction
}
