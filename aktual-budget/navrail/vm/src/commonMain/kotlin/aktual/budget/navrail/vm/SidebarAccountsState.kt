package aktual.budget.navrail.vm

import aktual.budget.model.AccountGroup
import aktual.budget.model.AccountGroupId
import androidx.compose.runtime.Immutable
import kotlin.jvm.JvmInline
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList

/**
 * A collapsible part of the sidebar's account list. [key] matches upstream's
 * `sidebar.accountsOpenState` keys, see
 * packages/desktop-client/src/components/sidebar/redesign/useSidebarCollapseState.ts
 */
@Immutable
@JvmInline
value class SidebarSection(val key: String) {
  companion object {
    val OnBudgetAccounts = SidebarSection("onbudget")
    val OffBudgetAccounts = SidebarSection("offbudget")
    val ClosedAccounts = SidebarSection("closed")

    fun group(id: AccountGroupId, offBudget: Boolean): SidebarSection =
      SidebarSection("group-${id.value}-${if (offBudget) "off" else "on"}")

    fun of(group: AccountGroup): SidebarSection =
      when (group) {
        OnBudget -> OnBudgetAccounts
        OffBudget -> OffBudgetAccounts
        Closed -> ClosedAccounts
      }
  }
}

/**
 * [accounts] has the search applied. [search] is null while the search field is hidden.
 * [openSections] holds what's been toggled; anything else is open, apart from closed accounts.
 */
@Immutable
data class SidebarAccountsState(
  val accounts: DrawerAccounts = DrawerAccounts(),
  val search: String? = null,
  val openSections: ImmutableMap<String, Boolean> = persistentMapOf(),
  // Before the search, so it covers every group
  val toggleableSections: ImmutableList<SidebarSection> = persistentListOf(),
) {
  val isSearching: Boolean
    get() = !search.isNullOrBlank()

  val allOpen: Boolean
    get() = toggleableSections.all(::isOpen)

  // Searching opens everything, so matches aren't hidden
  fun isOpen(section: SidebarSection): Boolean =
    isSearching || openSections[section.key] ?: (section != ClosedAccounts)
}

// Expand/collapse all covers the on/off budget sections and their groups, as upstream
internal fun DrawerAccounts.toggleableSections(): ImmutableList<SidebarSection> = buildList {
  add(OnBudgetAccounts)
  add(OffBudgetAccounts)
  onBudget.buckets.forEach { bucket ->
    bucket.group?.let { add(SidebarSection.group(it.id, offBudget = false)) }
  }
  offBudget.buckets.forEach { bucket ->
    bucket.group?.let { add(SidebarSection.group(it.id, offBudget = true)) }
  }
}
  .toImmutableList()
